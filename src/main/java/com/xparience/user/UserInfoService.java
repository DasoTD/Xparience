package com.xparience.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.xparience.profile.Profile;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.dto.UpdateProfileRequest;
import com.xparience.user.dto.UserInfoResponse;
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
public class UserInfoService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final Cloudinary cloudinary;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserInfoResponse getUserInfo() {
        User user = getCurrentUser();
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return toUserInfoResponse(user, profile);
    }

    @Transactional
    public UserInfoResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGenderIdentity() != null) profile.setGenderIdentity(request.getGenderIdentity());
        if (request.getGenderPreference() != null) profile.setGenderPreference(request.getGenderPreference());
        if (request.getRelationshipGoal() != null) profile.setRelationshipGoal(request.getRelationshipGoal());
        if (request.getReasonForJoining() != null) profile.setReasonForJoining(request.getReasonForJoining());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getValues() != null) profile.setValues(request.getValues());
        if (request.getPreferences() != null) profile.setPreferences(request.getPreferences());
        if (request.getNonNegotiable1() != null) profile.setNonNegotiable1(request.getNonNegotiable1());
        if (request.getNonNegotiable2() != null) profile.setNonNegotiable2(request.getNonNegotiable2());
        if (request.getNonNegotiable3() != null) profile.setNonNegotiable3(request.getNonNegotiable3());
        if (request.getDietStyle() != null) profile.setDietStyle(request.getDietStyle());
        if (request.getHealthGoals() != null) profile.setHealthGoals(request.getHealthGoals());
        if (request.getAllergiesOrRestrictions() != null)
            profile.setAllergiesOrRestrictions(request.getAllergiesOrRestrictions());
        if (request.getIdealWeekendActivity() != null)
            profile.setIdealWeekendActivity(request.getIdealWeekendActivity());
        if (request.getFictionalDinnerGuest() != null)
            profile.setFictionalDinnerGuest(request.getFictionalDinnerGuest());
        if (request.getThreeWordsFromFriend() != null)
            profile.setThreeWordsFromFriend(request.getThreeWordsFromFriend());
        if (request.getSurprisingPassion() != null)
            profile.setSurprisingPassion(request.getSurprisingPassion());
        if (request.getEmotionalIntelligence() != null)
            profile.setEmotionalIntelligence(request.getEmotionalIntelligence());

        profileRepository.save(profile);
        return toUserInfoResponse(user, profile);
    }

    @Transactional
    public UserInfoResponse updateImages(MultipartFile profilePicture,
                                         List<MultipartFile> additionalImages) {
        User user = getCurrentUser();
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String url = uploadToCloudinary(profilePicture, "xparience/profiles");
            profile.setProfilePictureUrl(url);
        }

        if (additionalImages != null && !additionalImages.isEmpty()) {
            if (additionalImages.size() > 6) {
                throw new RuntimeException("Maximum 6 additional images allowed");
            }
            List<String> urls = new ArrayList<>();
            for (MultipartFile img : additionalImages) {
                if (!img.isEmpty()) {
                    urls.add(uploadToCloudinary(img, "xparience/gallery"));
                }
            }
            profile.setAdditionalImageUrls(urls);
        }

        profileRepository.save(profile);
        return toUserInfoResponse(user, profile);
    }

    private String uploadToCloudinary(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(), ObjectUtils.asMap("folder", folder));
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    private UserInfoResponse toUserInfoResponse(User user, Profile profile) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(profile.getFullName());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setGenderIdentity(profile.getGenderIdentity());
        response.setGenderPreference(profile.getGenderPreference());
        response.setRelationshipGoal(profile.getRelationshipGoal());
        response.setReasonForJoining(profile.getReasonForJoining());
        response.setCity(profile.getCity());
        response.setProfilePictureUrl(profile.getProfilePictureUrl());
        response.setAdditionalImageUrls(profile.getAdditionalImageUrls());
        response.setBio(profile.getBio());
        response.setValues(profile.getValues());
        response.setPreferences(profile.getPreferences());
        response.setNonNegotiable1(profile.getNonNegotiable1());
        response.setNonNegotiable2(profile.getNonNegotiable2());
        response.setNonNegotiable3(profile.getNonNegotiable3());
        response.setDietStyle(profile.getDietStyle());
        response.setHealthGoals(profile.getHealthGoals());
        response.setAllergiesOrRestrictions(profile.getAllergiesOrRestrictions());
        response.setIdealWeekendActivity(profile.getIdealWeekendActivity());
        response.setFictionalDinnerGuest(profile.getFictionalDinnerGuest());
        response.setThreeWordsFromFriend(profile.getThreeWordsFromFriend());
        response.setSurprisingPassion(profile.getSurprisingPassion());
        response.setEmotionalIntelligence(profile.getEmotionalIntelligence());
        return response;
    }
}