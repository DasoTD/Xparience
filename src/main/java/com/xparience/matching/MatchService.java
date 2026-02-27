package com.xparience.matching;

import com.xparience.matching.dto.MatchActionRequest;
import com.xparience.matching.dto.MatchResponse;
import com.xparience.profile.Profile;
import com.xparience.profile.ProfileRepository;
import com.xparience.subscription.Subscription;
import com.xparience.subscription.SubscriptionPlan;
import com.xparience.subscription.SubscriptionRepository;
import com.xparience.subscription.SubscriptionStatus;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SubscriptionRepository subscriptionRepository;

    private static final double MIN_COMPATIBILITY_SCORE = 60.0;
    private static final int MATCH_EXPIRY_HOURS = 24;
    private static final int NO_REPEAT_DAYS = 30;

    private static final double PERSONALITY_WEIGHT = 0.30;
    private static final double INTERESTS_WEIGHT = 0.20;
    private static final double NUTRITIONAL_WEIGHT = 0.15;
    private static final double NON_NEGOTIABLE_WEIGHT = 0.20;
    private static final double RELATIONSHIP_GOAL_WEIGHT = 0.15;
    private static final int DEFAULT_RADIUS_KM = 50;
    private static final int MAX_DAILY_DELIVERY = 3;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public List<MatchResponse> getAiDailyMatches() {
        User user = getCurrentUser();

        expirePendingMatches();

        LocalDateTime now = LocalDateTime.now();
        List<Match> matches = matchRepository.findPendingAiMatchesByUserId(user.getId(), now);
        int allowedForToday = getRemainingDailyMatchAllowance(user.getId());

        if (allowedForToday <= 0) {
            return List.of();
        }

        return matches.stream()
                .filter(match -> isEligibleMatch(match, user))
            .sorted(Comparator
                .comparing((Match match) -> isVerifiedCounterpart(match, user)).reversed()
                .thenComparing(Match::getOverallCompatibilityScore, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(Math.min(MAX_DAILY_DELIVERY, allowedForToday))
                .map(match -> buildMatchResponse(match, user))
                .collect(Collectors.toList());
    }

    public List<MatchResponse> getAcceptedMatches() {
        User user = getCurrentUser();
        List<Match> matches = matchRepository.findByUserIdAndStatus(user.getId(), MatchStatus.ACCEPTED);
        return matches.stream()
                .map(match -> buildMatchResponse(match, user))
                .collect(Collectors.toList());
    }

    @Transactional
    public String respondToMatch(Long matchId, MatchActionRequest request) {
        User user = getCurrentUser();
        expirePendingMatches();

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.getUserOne().getId().equals(user.getId()) &&
            !match.getUserTwo().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to respond to this match");
        }

        if (request.getAction() != MatchStatus.ACCEPTED && request.getAction() != MatchStatus.REJECTED) {
            throw new RuntimeException("Only ACCEPTED or REJECTED actions are allowed");
        }

        if (match.getStatus() == MatchStatus.EXPIRED || isExpired(match)) {
            match.setStatus(MatchStatus.EXPIRED);
            matchRepository.save(match);
            throw new RuntimeException("Match has expired and can no longer be responded to");
        }

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new RuntimeException("Match has already been responded to");
        }

        if (!isEligibleMatch(match, user)) {
            throw new RuntimeException("This match no longer meets eligibility requirements");
        }

        match.setStatus(request.getAction());
        match.setRespondedAt(LocalDateTime.now());
        matchRepository.save(match);

        return request.getAction() == MatchStatus.ACCEPTED
                ? "Match accepted! You can now start chatting."
                : "Match rejected.";
    }

    @Transactional
    protected void expirePendingMatches() {
        matchRepository.expirePendingMatches(LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void runDailyMatchMaintenance() {
        expirePendingMatches();

        List<Match> pendingAiMatches = matchRepository.findAll().stream()
                .filter(match -> match.getStatus() == MatchStatus.PENDING && match.isAiGenerated())
                .collect(Collectors.toList());

        for (Match match : pendingAiMatches) {
            Profile userOneProfile = profileRepository.findByUserId(match.getUserOne().getId()).orElse(null);
            Profile userTwoProfile = profileRepository.findByUserId(match.getUserTwo().getId()).orElse(null);
            if (userOneProfile == null || userTwoProfile == null) {
                continue;
            }
            applyCompatibilityScores(match, userOneProfile, userTwoProfile);
            if (match.getExpiresAt() == null) {
                LocalDateTime reference = match.getMatchedAt() != null ? match.getMatchedAt() : LocalDateTime.now();
                match.setExpiresAt(reference.plusHours(MATCH_EXPIRY_HOURS));
            }
            matchRepository.save(match);
        }

        generateDailyMatchesForAllUsers();
    }

    private int getRemainingDailyMatchAllowance(Long userId) {
        SubscriptionPlan plan = getUserPlan(userId);
        LocalDate currentDate = LocalDate.now();
        LocalDateTime startOfDay = currentDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long generatedToday = matchRepository.countAiMatchesForUserBetween(userId, startOfDay, endOfDay);

        LocalDate weekStartDate = currentDate.with(DayOfWeek.MONDAY);
        LocalDate weekEndDateExclusive = weekStartDate.plusDays(7);
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekEndDateExclusive.atStartOfDay();
        long generatedThisWeek = matchRepository.countAiMatchesForUserBetween(userId, weekStart, weekEnd);

        int weeklyLimit = plan.getWeeklyMatches();
        int remainingWeekly = Math.max(0, weeklyLimit - (int) generatedThisWeek);
        if (remainingWeekly == 0) {
            return 0;
        }

        long remainingDays = currentDate.until(weekEndDateExclusive).getDays();
        if (remainingDays <= 0) {
            remainingDays = 1;
        }

        int dailyTarget = (int) Math.ceil((double) remainingWeekly / remainingDays);
        int dailyLimit = Math.max(1, Math.min(MAX_DAILY_DELIVERY, dailyTarget));

        return Math.max(0, dailyLimit - (int) generatedToday);
    }

    private SubscriptionPlan getUserPlan(Long userId) {
        return subscriptionRepository.findByUserId(userId)
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE
                        || subscription.getStatus() == SubscriptionStatus.TRIAL)
                .map(Subscription::getPlan)
                .orElse(SubscriptionPlan.FREE);
    }

    private boolean isEligibleMatch(Match match, User currentUser) {
        if (match.getStatus() != MatchStatus.PENDING || isExpired(match)) {
            return false;
        }

        User otherUser = match.getUserOne().getId().equals(currentUser.getId())
                ? match.getUserTwo()
                : match.getUserOne();

        Profile currentProfile = profileRepository.findByUserId(currentUser.getId()).orElse(null);
        Profile otherProfile = profileRepository.findByUserId(otherUser.getId()).orElse(null);

        if (currentProfile == null || otherProfile == null) {
            return false;
        }

        applyCompatibilityScores(match, currentProfile, otherProfile);
        if (match.getOverallCompatibilityScore() == null
                || match.getOverallCompatibilityScore() < MIN_COMPATIBILITY_SCORE) {
            return false;
        }

        if (hasRecentPairing(match, currentUser.getId(), otherUser.getId())) {
            return false;
        }

        if (!isWithinLocationRadius(currentProfile, otherProfile)) {
            return false;
        }

        return !hasNonNegotiableConflict(currentProfile, otherProfile);
    }

    private void generateDailyMatchesForAllUsers() {
        List<User> users = userRepository.findAll().stream()
                .filter(User::isEnabled)
                .toList();

        for (User user : users) {
            Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
            if (profile == null || !profile.isReviewSubmitted()) {
                continue;
            }

            ZoneId zoneId = resolveZone(profile);
            ZonedDateTime nowInZone = ZonedDateTime.now(zoneId);
            if (nowInZone.getHour() != 9) {
                continue;
            }

            int remainingAllowance = getRemainingDailyMatchAllowance(user.getId());
            if (remainingAllowance <= 0) {
                continue;
            }

            generateMatchesForUser(user, profile, remainingAllowance);
        }
    }

    private void generateMatchesForUser(User sourceUser, Profile sourceProfile, int limit) {
        LocalDateTime now = LocalDateTime.now();

        List<MatchCandidate> candidates = userRepository.findAll().stream()
                .filter(otherUser -> !Objects.equals(otherUser.getId(), sourceUser.getId()))
                .filter(User::isEnabled)
                .map(otherUser -> {
                    Profile otherProfile = profileRepository.findByUserId(otherUser.getId()).orElse(null);
                    if (otherProfile == null || !otherProfile.isReviewSubmitted()) {
                        return null;
                    }

                    Match probe = new Match();
                    probe.setUserOne(sourceUser);
                    probe.setUserTwo(otherUser);

                    if (hasRecentPairing(probe, sourceUser.getId(), otherUser.getId())) {
                        return null;
                    }

                    if (!isWithinLocationRadius(sourceProfile, otherProfile)) {
                        return null;
                    }

                    if (hasNonNegotiableConflict(sourceProfile, otherProfile)) {
                        return null;
                    }

                    applyCompatibilityScores(probe, sourceProfile, otherProfile);
                    Double score = probe.getOverallCompatibilityScore();
                    if (score == null || score < MIN_COMPATIBILITY_SCORE) {
                        return null;
                    }

                    return new MatchCandidate(otherUser, otherProfile, probe);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing((MatchCandidate candidate) -> candidate.otherProfile().isIdentityVerified()).reversed()
                        .thenComparing(candidate -> candidate.scoredMatch().getOverallCompatibilityScore(), Comparator.reverseOrder()))
                .toList();

        int created = 0;
        for (MatchCandidate candidate : candidates) {
            if (created >= limit) {
                break;
            }

            Match newMatch = candidate.scoredMatch();
            newMatch.setUserOne(sourceUser);
            newMatch.setUserTwo(candidate.otherUser());
            newMatch.setStatus(MatchStatus.PENDING);
            newMatch.setAiGenerated(true);
            newMatch.setMatchedAt(now);
            newMatch.setExpiresAt(now.plusHours(MATCH_EXPIRY_HOURS));

            matchRepository.save(newMatch);
            created++;
        }
    }

    private void applyCompatibilityScores(Match match, Profile currentProfile, Profile otherProfile) {
        double personalityScore = calculatePersonalityScore(currentProfile, otherProfile);
        double interestsScore = calculateInterestsScore(currentProfile, otherProfile);
        double nutritionalScore = calculateNutritionalScore(currentProfile, otherProfile);
        double nonNegotiableScore = hasNonNegotiableConflict(currentProfile, otherProfile) ? 0.0 : 100.0;
        double relationshipGoalScore = calculateRelationshipGoalScore(currentProfile, otherProfile);

        double overall = personalityScore * PERSONALITY_WEIGHT
                + interestsScore * INTERESTS_WEIGHT
                + nutritionalScore * NUTRITIONAL_WEIGHT
                + nonNegotiableScore * NON_NEGOTIABLE_WEIGHT
                + relationshipGoalScore * RELATIONSHIP_GOAL_WEIGHT;

        match.setOverallCompatibilityScore(round(overall));
        match.setProfileMatchScore(round((personalityScore + relationshipGoalScore) / 2.0));
        match.setNutritionalValueScore(round(nutritionalScore));

        Set<String> shared = getSharedInterests(currentProfile, otherProfile);
        match.setSharedInterests(String.join(",", shared));
    }

    private double calculatePersonalityScore(Profile left, Profile right) {
        int total = 0;
        int matches = 0;

        String[] leftAnswers = {
                normalize(left.getIdealWeekendActivity()),
                normalize(left.getFictionalDinnerGuest()),
                normalize(left.getThreeWordsFromFriend()),
                normalize(left.getSurprisingPassion()),
                normalize(left.getEmotionalIntelligence())
        };

        String[] rightAnswers = {
                normalize(right.getIdealWeekendActivity()),
                normalize(right.getFictionalDinnerGuest()),
                normalize(right.getThreeWordsFromFriend()),
                normalize(right.getSurprisingPassion()),
                normalize(right.getEmotionalIntelligence())
        };

        for (int i = 0; i < leftAnswers.length; i++) {
            if (leftAnswers[i] == null || rightAnswers[i] == null) {
                continue;
            }
            total++;
            if (leftAnswers[i].equals(rightAnswers[i])) {
                matches++;
            }
        }

        if (total == 0) {
            return 50.0;
        }

        return ((double) matches / total) * 100.0;
    }

    private double calculateInterestsScore(Profile left, Profile right) {
        Set<String> leftSet = normalizeToSet(left.getPreferences());
        Set<String> rightSet = normalizeToSet(right.getPreferences());
        if (leftSet.isEmpty() || rightSet.isEmpty()) {
            return 40.0;
        }

        Set<String> intersection = new HashSet<>(leftSet);
        intersection.retainAll(rightSet);

        Set<String> union = new HashSet<>(leftSet);
        union.addAll(rightSet);

        if (union.isEmpty()) {
            return 0.0;
        }

        return ((double) intersection.size() / union.size()) * 100.0;
    }

    private Set<String> getSharedInterests(Profile left, Profile right) {
        Set<String> leftSet = normalizeToSet(left.getPreferences());
        Set<String> rightSet = normalizeToSet(right.getPreferences());
        leftSet.retainAll(rightSet);
        return leftSet;
    }

    private double calculateNutritionalScore(Profile left, Profile right) {
        int total = 0;
        int matches = 0;

        String leftDiet = normalize(left.getDietStyle());
        String rightDiet = normalize(right.getDietStyle());
        if (leftDiet != null && rightDiet != null) {
            total++;
            if (leftDiet.equals(rightDiet)) {
                matches++;
            }
        }

        String leftGoal = normalize(left.getHealthGoals());
        String rightGoal = normalize(right.getHealthGoals());
        if (leftGoal != null && rightGoal != null) {
            total++;
            if (leftGoal.equals(rightGoal)) {
                matches++;
            }
        }

        if (total == 0) {
            return 50.0;
        }

        return ((double) matches / total) * 100.0;
    }

    private double calculateRelationshipGoalScore(Profile left, Profile right) {
        String leftGoal = normalize(left.getRelationshipGoal());
        String rightGoal = normalize(right.getRelationshipGoal());
        if (leftGoal == null || rightGoal == null) {
            return 40.0;
        }
        return leftGoal.equals(rightGoal) ? 100.0 : 0.0;
    }

    private Set<String> normalizeToSet(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new HashSet<>();
        }
        return values.stream()
                .map(this::normalize)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean hasRecentPairing(Match match, Long userId, Long otherUserId) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(NO_REPEAT_DAYS);

        if (match.getId() == null) {
            return matchRepository.existsMatchBetweenUsersSince(userId, otherUserId, cutoff);
        }

        return matchRepository.existsOtherMatchBetweenUsersSince(userId, otherUserId, cutoff, match.getId());
    }

    private boolean hasNonNegotiableConflict(Profile currentProfile, Profile matchedProfile) {
        if (hasProfileKeywordConflict(currentProfile, matchedProfile)) {
            return true;
        }

        String currentGoal = normalize(currentProfile.getRelationshipGoal());
        String matchedGoal = normalize(matchedProfile.getRelationshipGoal());
        if (currentGoal != null && matchedGoal != null && !Objects.equals(currentGoal, matchedGoal)) {
            return true;
        }

        return isDietConflict(currentProfile.getDietStyle(), matchedProfile.getDietStyle());
    }

    private boolean hasProfileKeywordConflict(Profile sourceProfile, Profile targetProfile) {
        String targetCorpus = String.join(" ",
                valueOrEmpty(targetProfile.getBio()),
                valueOrEmpty(targetProfile.getValues()),
                valueOrEmpty(targetProfile.getReasonForJoining()),
                valueOrEmpty(targetProfile.getDietStyle()))
                .toLowerCase(Locale.ROOT);

        return containsConflictKeyword(sourceProfile, sourceProfile.getNonNegotiable1(), targetCorpus, targetProfile)
                || containsConflictKeyword(sourceProfile, sourceProfile.getNonNegotiable2(), targetCorpus, targetProfile)
                || containsConflictKeyword(sourceProfile, sourceProfile.getNonNegotiable3(), targetCorpus, targetProfile);
    }

    private boolean containsConflictKeyword(Profile sourceProfile,
                                            String nonNegotiable,
                                            String targetCorpus,
                                            Profile targetProfile) {
        String rule = normalize(nonNegotiable);
        if (rule == null) {
            return false;
        }

        if ((rule.contains("verified") || rule.contains("verification")) && !targetProfile.isIdentityVerified()) {
            return true;
        }

        if ((rule.contains("same city") || rule.contains("local"))
                && sourceProfile.getCity() != null
                && targetProfile.getCity() != null
                && !Objects.equals(normalize(sourceProfile.getCity()), normalize(targetProfile.getCity()))) {
            return true;
        }

        String[] tokens = rule.split("[^a-z0-9]+");
        for (String token : tokens) {
            if (token.length() < 4) {
                continue;
            }
            if (targetCorpus.contains(token)) {
                return false;
            }
        }

        return false;
    }

    private boolean isDietConflict(String leftDiet, String rightDiet) {
        String left = normalize(leftDiet);
        String right = normalize(rightDiet);

        if (left == null || right == null) {
            return false;
        }

        return (left.contains("vegan") && (right.contains("carnivore") || right.contains("animal-based")))
                || (right.contains("vegan") && (left.contains("carnivore") || left.contains("animal-based")));
    }

    private boolean isExpired(Match match) {
        if (match.getExpiresAt() != null) {
            return !match.getExpiresAt().isAfter(LocalDateTime.now());
        }

        LocalDateTime reference = match.getMatchedAt() != null ? match.getMatchedAt() : match.getCreatedAt();
        if (reference == null) {
            return false;
        }

        return !reference.plusHours(MATCH_EXPIRY_HOURS).isAfter(LocalDateTime.now());
    }

    private boolean isWithinLocationRadius(Profile currentProfile, Profile otherProfile) {
        if (currentProfile.getLatitude() != null && currentProfile.getLongitude() != null
                && otherProfile.getLatitude() != null && otherProfile.getLongitude() != null) {
            double distanceKm = haversineKm(
                    currentProfile.getLatitude(),
                    currentProfile.getLongitude(),
                    otherProfile.getLatitude(),
                    otherProfile.getLongitude());

            int currentRadius = currentProfile.getMatchRadiusKm() == null ? DEFAULT_RADIUS_KM : currentProfile.getMatchRadiusKm();
            int otherRadius = otherProfile.getMatchRadiusKm() == null ? DEFAULT_RADIUS_KM : otherProfile.getMatchRadiusKm();
            int effectiveRadius = Math.min(currentRadius, otherRadius);

            return distanceKm <= effectiveRadius;
        }

        String currentCity = normalize(currentProfile.getCity());
        String otherCity = normalize(otherProfile.getCity());
        if (currentCity != null && otherCity != null) {
            return currentCity.equals(otherCity);
        }

        return false;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private ZoneId resolveZone(Profile profile) {
        try {
            if (profile.getTimezone() != null && !profile.getTimezone().isBlank()) {
                return ZoneId.of(profile.getTimezone());
            }
        } catch (Exception ignored) {
        }
        return ZoneId.systemDefault();
    }

    private boolean isVerifiedCounterpart(Match match, User currentUser) {
        User counterpart = match.getUserOne().getId().equals(currentUser.getId())
                ? match.getUserTwo()
                : match.getUserOne();
        Profile counterpartProfile = profileRepository.findByUserId(counterpart.getId()).orElse(null);
        return counterpartProfile != null && counterpartProfile.isIdentityVerified();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private MatchResponse buildMatchResponse(Match match, User currentUser) {
        User matchedUser = match.getUserOne().getId().equals(currentUser.getId())
                ? match.getUserTwo()
                : match.getUserOne();

        Profile profile = profileRepository.findByUserId(matchedUser.getId()).orElse(null);

        int age = 0;
        List<String> sharedInterests = List.of();

        if (profile != null && profile.getDateOfBirth() != null) {
            age = Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears();
        }

        if (match.getSharedInterests() != null) {
            sharedInterests = Arrays.asList(match.getSharedInterests().split(","));
        }

        MatchResponse response = new MatchResponse();
        response.setMatchId(match.getId());
        response.setMatchedUserId(matchedUser.getId());
        response.setFullName(profile != null ? profile.getFullName() : matchedUser.getEmail());
        response.setAge(age);
        response.setProfilePictureUrl(profile != null ? profile.getProfilePictureUrl() : null);
        response.setCity(profile != null ? profile.getCity() : null);
        response.setBio(profile != null ? profile.getBio() : null);
        response.setStatus(match.getStatus());
        response.setOverallCompatibilityScore(match.getOverallCompatibilityScore());
        response.setProfileMatchScore(match.getProfileMatchScore());
        response.setNutritionalValueScore(match.getNutritionalValueScore());
        response.setSharedInterests(sharedInterests);
        response.setDietStyle(profile != null ? profile.getDietStyle() : null);
        response.setAiGenerated(match.isAiGenerated());
        return response;
    }

    private record MatchCandidate(User otherUser, Profile otherProfile, Match scoredMatch) {
    }
}