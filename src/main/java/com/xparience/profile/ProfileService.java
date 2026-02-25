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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileService {

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
        Profile profile = getOrCreateProfile(user);

        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGenderIdentity(request.getGenderIdentity());
        profile.setGenderPreference(request.getGenderPreference());
        profile.setRelationshipGoal(request.getRelationshipGoal());
        profile.setReasonForJoining(request.getReasonForJoining());
        profile.setCity(request.getCity());
        profile.setBasicInfoComplete(true);

        profileRepository.save(profile);
        return "Basic information saved successfully";
    }

    @Transactional
    public String uploadImages(MultipartFile profilePicture, List<MultipartFile> additionalImages) {
        User user = getCurrentUser();
        Profile profile = getOrCreateProfile(user);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String profilePicUrl = uploadToCloudinary(profilePicture, "xparience/profiles");
            profile.setProfilePictureUrl(profilePicUrl);
        }

        if (additionalImages != null && !additionalImages.isEmpty()) {
            if (additionalImages.size() > 6) {
                throw new RuntimeException("Maximum 6 additional images allowed");
            }
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : additionalImages) {
                if (!image.isEmpty()) {
                    String url = uploadToCloudinary(image, "xparience/gallery");
                    imageUrls.add(url);
                }
            }
            profile.setAdditionalImageUrls(imageUrls);
        }

        profile.setImagesComplete(true);
        profileRepository.save(profile);
        return "Images uploaded successfully";
    }

    private String uploadToCloudinary(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder)
            );
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    @Transactional
    public String saveAboutYou(AboutYouRequest request) {
        User user = getCurrentUser();
        Profile profile = getOrCreateProfile(user);

        profile.setBio(request.getBio());
        profile.setValues(request.getValues());
        profile.setAboutYouComplete(true);

        profileRepository.save(profile);
        return "About you information saved successfully";
    }

    @Transactional
    public String savePreferences(PreferencesRequest request) {
        User user = getCurrentUser();
        Profile profile = getOrCreateProfile(user);

        profile.setPreferences(request.getPreferences());
        profile.setPreferencesComplete(true);

        profileRepository.save(profile);
        return "Preferences saved successfully";
    }

    @Transactional
    public String saveNonNegotiables(NonNegotiablesRequest request) {
        User user = getCurrentUser();
        Profile profile = getOrCreateProfile(user);

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
        Profile profile = getOrCreateProfile(user);

        profile.setDietStyle(request.getDietStyle());
        profile.setHealthGoals(request.getHealthGoals());
        profile.setAllergiesOrRestrictions(request.getAllergiesOrRestrictions());
        profile.setNutritionVibeComplete(true);

        profileRepository.save(profile);
        return "Nutrition vibe saved successfully";
    }

    @Transactional
    public String savePersonalityQuiz(PersonalityQuizRequest request) {
        User user = getCurrentUser();
        Profile profile = getOrCreateProfile(user);

        profile.setIdealWeekendActivity(request.idealWeekendActivity());
        profile.setFictionalDinnerGuest(request.fictionalDinnerGuest());
        profile.setThreeWordsFromFriend(request.threeWordsFromFriend());
        profile.setSurprisingPassion(request.surprisingPassion());
        profile.setEmotionalIntelligence(request.emotionalIntelligence());
        profile.setPersonalityQuizComplete(true);

        profileRepository.save(profile);
        return "Personality quiz saved successfully";
    }

    public ProfileCompletionResponse getCompletionStatus() {
        User user = getCurrentUser();
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElse(new Profile());

        int completedSteps = 0;
        int totalSteps = 7;

        if (profile.isBasicInfoComplete()) completedSteps++;
        if (profile.isImagesComplete()) completedSteps++;
        if (profile.isAboutYouComplete()) completedSteps++;
        if (profile.isPreferencesComplete()) completedSteps++;
        if (profile.isNonNegotiablesComplete()) completedSteps++;
        if (profile.isNutritionVibeComplete()) completedSteps++;
        if (profile.isPersonalityQuizComplete()) completedSteps++;

        int percentage = (int) ((completedSteps / (double) totalSteps) * 100);

        ProfileCompletionResponse response = new ProfileCompletionResponse();
        response.setBasicInfoComplete(profile.isBasicInfoComplete());
        response.setImagesComplete(profile.isImagesComplete());
        response.setAboutYouComplete(profile.isAboutYouComplete());
        response.setPreferencesComplete(profile.isPreferencesComplete());
        response.setNonNegotiablesComplete(profile.isNonNegotiablesComplete());
        response.setNutritionVibeComplete(profile.isNutritionVibeComplete());
        response.setPersonalityQuizComplete(profile.isPersonalityQuizComplete());
        response.setIdentityVerified(profile.isIdentityVerified());
        response.setCompletionPercentage(percentage);
        return response;
    }

    @Transactional
    public String verifyIdentity() {
        User user = getCurrentUser();
        Profile profile = getOrCreateProfile(user);

        // Placeholder — integrate with a real identity verification provider
        profile.setIdentityVerified(true);
        profileRepository.save(profile);

        user.setIdentityVerified(true);
        userRepository.save(user);

        return "Identity verified successfully. Your profile is now complete!";
    }
}