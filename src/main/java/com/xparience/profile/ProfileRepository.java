package com.xparience.profile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // Find profile by user ID
    Optional<Profile> findByUserId(Long userId);

    // Check if profile exists for user
    boolean existsByUserId(Long userId);

    // Find profiles by city (for matching)
    List<Profile> findByCityIgnoreCase(String city);

    // Find fully complete profiles (all 7 steps done)
    @Query("SELECT p FROM Profile p WHERE " +
           "p.basicInfoComplete = true AND " +
           "p.imagesComplete = true AND " +
           "p.aboutYouComplete = true AND " +
           "p.preferencesComplete = true AND " +
           "p.nonNegotiablesComplete = true AND " +
           "p.nutritionVibeComplete = true AND " +
           "p.personalityQuizComplete = true AND " +
           "p.reviewSubmitted = true")
    Page<Profile> findAllCompleteProfiles(Pageable pageable);

    // Find profiles matching gender preference (for AI matching)
    @Query("SELECT p FROM Profile p WHERE " +
           "p.genderIdentity = :genderPreference " +
           "AND p.genderPreference = :genderIdentity " +
           "AND p.user.id != :currentUserId " +
           "AND p.basicInfoComplete = true")
    List<Profile> findCompatibleProfiles(
            @Param("genderPreference") String genderPreference,
            @Param("genderIdentity") String genderIdentity,
            @Param("currentUserId") Long currentUserId);

    // Update profile picture URL
    @Modifying
    @Query("UPDATE Profile p SET p.profilePictureUrl = :url WHERE p.user.id = :userId")
    void updateProfilePicture(@Param("userId") Long userId, @Param("url") String url);

    // Mark a step complete
    @Modifying
    @Query("UPDATE Profile p SET p.basicInfoComplete = true WHERE p.user.id = :userId")
    void markBasicInfoComplete(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Profile p SET p.imagesComplete = true WHERE p.user.id = :userId")
    void markImagesComplete(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Profile p SET p.personalityQuizComplete = true WHERE p.user.id = :userId")
    void markPersonalityQuizComplete(@Param("userId") Long userId);
}