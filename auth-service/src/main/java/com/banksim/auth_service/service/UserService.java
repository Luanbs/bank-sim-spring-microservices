package com.banksim.auth_service.service;

import com.banksim.auth_service.dto.UserProfileResponse;
import com.banksim.auth_service.entity.UserProfile;
import com.banksim.auth_service.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfileResponse getUserProfileByUserId(String id) {
        UserProfile userProfile = userProfileRepository.findByUser_Id(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UserProfile not found."));

        return userProfileToResponse(userProfile);
    }

    private UserProfileResponse userProfileToResponse(UserProfile userProfile) {
        return new UserProfileResponse(userProfile.getFullName(),
                userProfile.getEmail(),
                userProfile.getLocation());
    }
}
