package com.xparience.matching;

import com.xparience.matching.dto.MatchActionRequest;
import com.xparience.matching.dto.MatchResponse;
import com.xparience.profile.Profile;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<MatchResponse> getAiDailyMatches() {
        User user = getCurrentUser();
        List<Match> matches = matchRepository.findTop3AiMatchesByUserId(user.getId());
        return matches.stream()
                .limit(3)
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
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.getUserOne().getId().equals(user.getId()) &&
            !match.getUserTwo().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to respond to this match");
        }

        match.setStatus(request.getAction());
        matchRepository.save(match);

        return request.getAction() == MatchStatus.ACCEPTED
                ? "Match accepted! You can now start chatting."
                : "Match rejected.";
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
}