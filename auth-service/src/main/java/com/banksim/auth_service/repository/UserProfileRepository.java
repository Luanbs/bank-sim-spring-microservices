package com.banksim.auth_service.repository;

import com.banksim.auth_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    boolean existsByEmail(String email);

    Optional<UserProfile> findByUser_Id(UUID uuid);
}
