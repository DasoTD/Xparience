package com.xparience.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    List<ProfileImage> findByProfileIdOrderByPositionAsc(Long profileId);

    @Modifying
    @Query("DELETE FROM ProfileImage pi WHERE pi.profile.id = :profileId")
    void deleteAllByProfileId(@Param("profileId") Long profileId);

    int countByProfileId(Long profileId);
}