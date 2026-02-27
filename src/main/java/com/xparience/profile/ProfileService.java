package com.xparience.profile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.xparience.profile.dto.*;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int ONBOARDING_TOTAL_STEPS = 8;
    private static final int MIN_PROFILE_PHOTOS = 1;
    private static final int MAX_PROFILE_PHOTOS = 4;
    private static final long MAX_IMAGE_SIZE_BYTES = 8L * 1024 * 1024;
    private static final int MIN_IMAGE_DIMENSION = 480;
    private static final double BLUR_EDGE_VARIANCE_THRESHOLD = 20.0;
    private static final double SAME_PERSON_THRESHOLD = 0.55;
    private static final Set<String> NSFW_KEYWORDS = Set.of(
        "nude", "naked", "sex", "porn", "xxx", "adult", "explicit", "nsfw"
    );
    private static final Set<String> AI_IMAGE_KEYWORDS = Set.of(
        "ai", "generated", "synthetic", "midjourney", "dalle", "stable-diffusion", "sdxl"
    );

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Profile getOrCreateProfile(User user) {
        return profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Profile profile = new Profile();
                    profile.setUser(user);
                    return profile;
                });
    }

    @Transactional
    public String saveBasicInfo(BasicInfoRequest request) {
        User user = getCurrentUser();
        ensureOnboardingEligible(user);
        Profile profile = getOrCreateProfile(user);
        ensureStepAccess(profile, 1);

        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGenderIdentity(request.getGenderIdentity());
        profile.setGenderPreference(request.getGenderPreference());
        profile.setRelationshipGoal(request.getRelationshipGoal());
        profile.setReasonForJoining(request.getReasonForJoining());
        profile.setCity(request.getCity());
        profile.setLatitude(request.getLatitude());
        profile.setLongitude(request.getLongitude());
        profile.setMatchRadiusKm(request.getMatchRadiusKm() == null ? 50 : request.getMatchRadiusKm());
        profile.setTimezone(firstNonBlank(request.getTimezone(), ZoneId.systemDefault().getId()));
        profile.setBasicInfoComplete(true);

        profileRepository.save(profile);
        return "Basic information saved successfully";
    }

    @Transactional
    public String uploadImages(MultipartFile profilePicture, List<MultipartFile> additionalImages) {
        User user = getCurrentUser();
        ensureOnboardingEligible(user);
        Profile profile = getOrCreateProfile(user);
        ensureStepAccess(profile, 2);

        if ((profilePicture == null || profilePicture.isEmpty())
                && (profile.getProfilePictureUrl() == null || profile.getProfilePictureUrl().isBlank())) {
            throw new RuntimeException("Primary profile photo is required to complete this step");
        }

        int uploadedAdditionalCount = countNonEmpty(additionalImages);
        if (uploadedAdditionalCount > (MAX_PROFILE_PHOTOS - 1)) {
            throw new RuntimeException("You can upload up to 4 photos total (1 primary + up to 3 additional)");
        }

        int totalPhotosInRequest = (profilePicture != null && !profilePicture.isEmpty() ? 1 : 0) + uploadedAdditionalCount;
        if (totalPhotosInRequest < MIN_PROFILE_PHOTOS || totalPhotosInRequest > MAX_PROFILE_PHOTOS) {
            throw new RuntimeException("Upload between 1 and 4 photos");
        }

        BufferedImage primaryImageReference = null;
        if (profilePicture != null && !profilePicture.isEmpty()) {
            primaryImageReference = readImage(profilePicture, "Primary profile photo");
            validateImageForOnboarding(profilePicture, primaryImageReference, "Primary profile photo", null);
        }

        if (profilePicture != null && !profilePicture.isEmpty()) {
            UploadResult profileUpload = uploadToCloudinary(profilePicture, "xparience/profiles");
            String profilePicUrl = profileUpload.secureUrl();
            profile.setProfilePictureUrl(profilePicUrl);
            profile.setProfilePictureThumbnailUrl(profileUpload.thumbnailUrl());
        }

        if (additionalImages != null && !additionalImages.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            List<String> thumbnailUrls = new ArrayList<>();
            for (MultipartFile image : additionalImages) {
                if (!image.isEmpty()) {
                    BufferedImage candidate = readImage(image, "Additional profile photo");
                    validateImageForOnboarding(image, candidate, "Additional profile photo", primaryImageReference);
                    UploadResult upload = uploadToCloudinary(image, "xparience/gallery");
                    imageUrls.add(upload.secureUrl());
                    thumbnailUrls.add(upload.thumbnailUrl());
                }
            }
            profile.setAdditionalImageUrls(imageUrls);
            profile.setAdditionalImageThumbnailUrls(thumbnailUrls);
        }

        profile.setImagesComplete(true);
        profileRepository.save(profile);
        return "Images uploaded successfully";
    }

    private UploadResult uploadToCloudinary(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder)
            );

            String secureUrl = result.get("secure_url").toString();
            String publicId = result.get("public_id") == null ? null : result.get("public_id").toString();

            String thumbnailUrl = publicId == null
                    ? secureUrl
                    : cloudinary.url()
                    .transformation(new com.cloudinary.Transformation<>().width(240).height(240).crop("fill").quality("auto"))
                    .generate(publicId);

            return new UploadResult(secureUrl, thumbnailUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    @Transactional
    public String saveAboutYou(AboutYouRequest request) {
        User user = getCurrentUser();
        ensureOnboardingEligible(user);
        Profile profile = getOrCreateProfile(user);
        ensureStepAccess(profile, 3);

        profile.setBio(request.getBio());
        profile.setValues(request.getValues());
        profile.setAboutYouComplete(true);

        profileRepository.save(profile);
        return "About you information saved successfully";
    }

    @Transactional
    public String savePreferences(PreferencesRequest request) {
        User user = getCurrentUser();
        ensureOnboardingEligible(user);
        Profile profile = getOrCreateProfile(user);
        ensureStepAccess(profile, 4);

        profile.setPreferences(request.getPreferences());
        profile.setPreferencesComplete(true);

        profileRepository.save(profile);
        return "Preferences saved successfully";
    }

    @Transactional
    public String saveNonNegotiables(NonNegotiablesRequest request) {
        User user = getCurrentUser();
        ensureOnboardingEligible(user);
        Profile profile = getOrCreateProfile(user);
        ensureStepAccess(profile, 5);

        profile.setNonNegotiable1(request.getNonNegotiable1());
        profile.setNonNegotiable2(request.getNonNegotiable2());
        profile.setNonNegotiable3(request.getNonNegotiable3());
        profile.setNonNegotiablesComplete(true);

        profileRepository.save(profile);
        return "Non-negotiables saved successfully";
    }

    @Transactional
    public String saveNutritionVibe(NutritionVibeRequest request) {
        User user = getCurrentUser();
        ensureOnboardingEligible(user);
        Profile profile = getOrCreateProfile(user);
        ensureStepAccess(profile, 6);

        profile.setDietStyle(request.getDietStyle());
        profile.setCookingFrequency(request.getCookingFrequency());
        profile.setHealthGoals(request.getHealthGoals());
        profile.setAllergiesOrRestrictions(request.getAllergiesOrRestrictions());
        profile.setNutritionVibeComplete(true);

        profileRepository.save(profile);
        return "Nutrition vibe saved successfully";
    }

    @Transactional
    public String savePersonalityQuiz(PersonalityQuizRequest request) {
        User user = getCurrentUser();
        ensureOnboardingEligible(user);
        Profile profile = getOrCreateProfile(user);
        ensureStepAccess(profile, 7);

        List<String> sanitizedAnswers = request.answers().stream()
            .map(this::sanitizeAnswer)
            .collect(Collectors.toList());

        profile.setPersonalityQuizAnswers(sanitizedAnswers);

        profile.setIdealWeekendActivity(firstNonBlank(request.idealWeekendActivity(), sanitizedAnswers.get(0)));
        profile.setFictionalDinnerGuest(firstNonBlank(request.fictionalDinnerGuest(), sanitizedAnswers.get(1)));
        profile.setThreeWordsFromFriend(firstNonBlank(request.threeWordsFromFriend(), sanitizedAnswers.get(2)));
        profile.setSurprisingPassion(firstNonBlank(request.surprisingPassion(), sanitizedAnswers.get(3)));
        profile.setEmotionalIntelligence(firstNonBlank(request.emotionalIntelligence(), sanitizedAnswers.get(4)));
        profile.setPersonalityQuizComplete(true);

        profileRepository.save(profile);
        return "Personality quiz saved successfully";
    }

    @Transactional
    public String reviewAndSubmit(ReviewSubmitRequest request) {
        User user = getCurrentUser();
        ensureOnboardingEligible(user);
        Profile profile = getOrCreateProfile(user);
        ensureStepAccess(profile, 8);

        if (!profile.isPersonalityQuizComplete()) {
            throw new RuntimeException("Complete Step 7 (Personality Quiz) before submitting");
        }

        profile.setReviewSubmitted(true);
        profileRepository.save(profile);
        return "Profile review submitted successfully";
    }

    public ProfileCompletionResponse getCompletionStatus() {
        User user = getCurrentUser();
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElse(new Profile());

        int completedSteps = getLastCompletedStep(profile);
        int totalSteps = ONBOARDING_TOTAL_STEPS;
        int percentage = (int) ((completedSteps / (double) totalSteps) * 100);
        boolean onboardingRequired = !isOnboardingComplete(profile);
        int nextStep = onboardingRequired ? Math.min(completedSteps + 1, ONBOARDING_TOTAL_STEPS) : ONBOARDING_TOTAL_STEPS;

        ProfileCompletionResponse response = new ProfileCompletionResponse();
        response.setBasicInfoComplete(profile.isBasicInfoComplete());
        response.setImagesComplete(profile.isImagesComplete());
        response.setAboutYouComplete(profile.isAboutYouComplete());
        response.setPreferencesComplete(profile.isPreferencesComplete());
        response.setNonNegotiablesComplete(profile.isNonNegotiablesComplete());
        response.setNutritionVibeComplete(profile.isNutritionVibeComplete());
        response.setPersonalityQuizComplete(profile.isPersonalityQuizComplete());
        response.setReviewSubmitted(profile.isReviewSubmitted());
        response.setIdentityVerified(profile.isIdentityVerified());
        response.setCompletionPercentage(percentage);
        response.setRegistrationStatus(onboardingRequired ? "Registration in Progress" : "Active");
        response.setLastCompletedStep(completedSteps);
        response.setNextStep(nextStep);
        response.setOnboardingRequired(onboardingRequired);
        response.setResumeMessage(onboardingRequired
                ? "Welcome back, continue setting up your profile"
                : "Your profile is active");
        return response;
    }

    @Transactional
    public String verifyIdentity() {
        User user = getCurrentUser();
        ensureOnboardingEligible(user);
        Profile profile = getOrCreateProfile(user);
        if (!profile.isReviewSubmitted()) {
            throw new RuntimeException("Complete Step 8 (Review & Submit) before identity verification");
        }

        // Placeholder — integrate with a real identity verification provider
        profile.setIdentityVerified(true);
        profileRepository.save(profile);

        user.setIdentityVerified(true);
        userRepository.save(user);

        return "Identity verified successfully. Your profile is now complete!";
    }

    private void ensureOnboardingEligible(User user) {
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Verify your email or phone OTP before starting onboarding");
        }
    }

    private void ensureStepAccess(Profile profile, int requestedStep) {
        if (isOnboardingComplete(profile)) {
            return;
        }

        if (isStepComplete(profile, requestedStep)) {
            return;
        }

        int nextStep = Math.min(getLastCompletedStep(profile) + 1, ONBOARDING_TOTAL_STEPS);
        if (requestedStep != nextStep) {
            throw new RuntimeException("Complete Step " + nextStep + " before continuing");
        }
    }

    private int getLastCompletedStep(Profile profile) {
        int step = 0;
        if (profile.isBasicInfoComplete()) step = 1;
        if (profile.isImagesComplete()) step = 2;
        if (profile.isAboutYouComplete()) step = 3;
        if (profile.isPreferencesComplete()) step = 4;
        if (profile.isNonNegotiablesComplete()) step = 5;
        if (profile.isNutritionVibeComplete()) step = 6;
        if (profile.isPersonalityQuizComplete()) step = 7;
        if (profile.isReviewSubmitted()) step = 8;
        return step;
    }

    private boolean isStepComplete(Profile profile, int step) {
        return switch (step) {
            case 1 -> profile.isBasicInfoComplete();
            case 2 -> profile.isImagesComplete();
            case 3 -> profile.isAboutYouComplete();
            case 4 -> profile.isPreferencesComplete();
            case 5 -> profile.isNonNegotiablesComplete();
            case 6 -> profile.isNutritionVibeComplete();
            case 7 -> profile.isPersonalityQuizComplete();
            case 8 -> profile.isReviewSubmitted();
            default -> false;
        };
    }

    private boolean isOnboardingComplete(Profile profile) {
        return profile.isBasicInfoComplete()
                && profile.isImagesComplete()
                && profile.isAboutYouComplete()
                && profile.isPreferencesComplete()
                && profile.isNonNegotiablesComplete()
                && profile.isNutritionVibeComplete()
                && profile.isPersonalityQuizComplete()
                && profile.isReviewSubmitted();
    }

    private int countNonEmpty(List<MultipartFile> files) {
        if (files == null) {
            return 0;
        }
        int count = 0;
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private BufferedImage readImage(MultipartFile file, String label) {
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new RuntimeException(label + " must be under 8MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new RuntimeException(label + " must be a valid image file");
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new RuntimeException(label + " cannot be parsed as an image");
            }
            return image;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read image: " + ex.getMessage());
        }
    }

    private void validateImageForOnboarding(MultipartFile file,
                                            BufferedImage image,
                                            String label,
                                            BufferedImage referencePrimary) {
        if (image.getWidth() < MIN_IMAGE_DIMENSION || image.getHeight() < MIN_IMAGE_DIMENSION) {
            throw new RuntimeException(label + " must be at least 480x480");
        }

        String lowerName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (containsKeyword(lowerName, NSFW_KEYWORDS)) {
            throw new RuntimeException(label + " rejected by NSFW detection");
        }

        if (containsKeyword(lowerName, AI_IMAGE_KEYWORDS) || looksAiGenerated(image)) {
            throw new RuntimeException(label + " appears AI-generated and was rejected");
        }

        if (isBlurred(image)) {
            throw new RuntimeException(label + " appears too blurry");
        }

        if (!hasLikelyFace(image)) {
            throw new RuntimeException(label + " failed face detection");
        }

        if (referencePrimary != null) {
            double similarity = compareImages(referencePrimary, image);
            if (similarity < SAME_PERSON_THRESHOLD) {
                throw new RuntimeException(label + " does not pass same-person validation");
            }
        }
    }

    private boolean containsKeyword(String value, Set<String> keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasLikelyFace(BufferedImage image) {
        int centerXStart = image.getWidth() / 4;
        int centerXEnd = image.getWidth() * 3 / 4;
        int centerYStart = image.getHeight() / 5;
        int centerYEnd = image.getHeight() * 4 / 5;

        int samples = 0;
        int skinToneLike = 0;
        for (int y = centerYStart; y < centerYEnd; y += 4) {
            for (int x = centerXStart; x < centerXEnd; x += 4) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r > 95 && g > 40 && b > 20 && r > g && r > b && Math.abs(r - g) > 15) {
                    skinToneLike++;
                }
                samples++;
            }
        }

        return samples > 0 && ((double) skinToneLike / samples) > 0.02;
    }

    private boolean isBlurred(BufferedImage image) {
        List<Integer> edges = new ArrayList<>();
        for (int y = 1; y < image.getHeight() - 1; y += 2) {
            for (int x = 1; x < image.getWidth() - 1; x += 2) {
                int c = gray(image.getRGB(x, y));
                int r = gray(image.getRGB(x + 1, y));
                int d = gray(image.getRGB(x, y + 1));
                edges.add(Math.abs(c - r) + Math.abs(c - d));
            }
        }

        if (edges.isEmpty()) {
            return true;
        }

        double mean = edges.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = edges.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0.0);
        return variance < BLUR_EDGE_VARIANCE_THRESHOLD;
    }

    private boolean looksAiGenerated(BufferedImage image) {
        Set<Integer> sampledColors = new HashSet<>();
        List<Integer> edges = new ArrayList<>();
        for (int y = 1; y < image.getHeight() - 1; y += 6) {
            for (int x = 1; x < image.getWidth() - 1; x += 6) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                sampledColors.add(rgb);
                int c = gray(image.getRGB(x, y));
                int r = gray(image.getRGB(x + 1, y));
                edges.add(Math.abs(c - r));
            }
        }

        double avgEdge = edges.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        return sampledColors.size() < 200 && avgEdge < 10.0;
    }

    private double compareImages(BufferedImage left, BufferedImage right) {
        int bins = 16;
        double[] leftHist = histogram(left, bins);
        double[] rightHist = histogram(right, bins);

        double dot = 0.0;
        double leftNorm = 0.0;
        double rightNorm = 0.0;
        for (int i = 0; i < leftHist.length; i++) {
            dot += leftHist[i] * rightHist[i];
            leftNorm += leftHist[i] * leftHist[i];
            rightNorm += rightHist[i] * rightHist[i];
        }

        if (leftNorm == 0 || rightNorm == 0) {
            return 0.0;
        }

        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private double[] histogram(BufferedImage image, int bins) {
        double[] hist = new double[bins * 3];
        for (int y = 0; y < image.getHeight(); y += 4) {
            for (int x = 0; x < image.getWidth(); x += 4) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                hist[(r * bins) / 256]++;
                hist[bins + (g * bins) / 256]++;
                hist[(2 * bins) + (b * bins) / 256]++;
            }
        }
        return hist;
    }

    private int gray(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r * 30 + g * 59 + b * 11) / 100;
    }

    private String sanitizeAnswer(String value) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Each personality quiz answer must be provided");
        }
        String normalized = value.trim();
        if (normalized.length() > 300) {
            throw new RuntimeException("Each personality quiz answer must be 300 characters or less");
        }
        return normalized;
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback;
    }

    private record UploadResult(String secureUrl, String thumbnailUrl) {
    }
}