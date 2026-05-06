package com.banksim.auth_service.service;

import com.banksim.auth_service.dto.UserProfileResponse;
import com.banksim.auth_service.entity.User;
import com.banksim.auth_service.entity.UserProfile;
import com.banksim.auth_service.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserProfileByUserIdShouldReturnMappedResponse() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName("Luan Silva");
        profile.setEmail("luan@example.com");
        profile.setLocation("Sao Paulo");

        when(userProfileRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));

        UserProfileResponse response = userService.getUserProfileByUserId(userId.toString());

        assertEquals("Luan Silva", response.fullName());
        assertEquals("luan@example.com", response.email());
        assertEquals("Sao Paulo", response.location());
    }

    @Test
    void getUserProfileByUserIdShouldThrowNotFound() {
        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findByUser_Id(userId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.getUserProfileByUserId(userId.toString()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
