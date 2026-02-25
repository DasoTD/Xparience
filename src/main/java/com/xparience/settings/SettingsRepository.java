package com.xparience.settings;

import com.xparience.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Settings does not have its own entity — it aggregates
 * data from users, profiles, subscriptions and verification_records.
 * This repository delegates directly to the User entity
 * and is used for any settings-level queries on the users table.
 */
@Repository
public interface SettingsRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.enabled = true")
    Optional<User> findActiveUserById(@Param("id") Long id);

    @Query("SELECT u.emailVerified FROM User u WHERE u.id = :id")
    Boolean isEmailVerified(@Param("id") Long id);

    @Query("SELECT u.identityVerified FROM User u WHERE u.id = :id")
    Boolean isIdentityVerified(@Param("id") Long id);
}