package com.banksim.auth_service.controller;

import com.banksim.auth_service.dto.UserProfileResponse;
import com.banksim.auth_service.dto.request.UpdateProfileRequest;
import com.banksim.auth_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUserProfileByUserId(jwt.getSubject()));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/profile")
    public ResponseEntity<Void> updateUserProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest updatedUserProfile){

        return ResponseEntity.ok().build();
    }

}
