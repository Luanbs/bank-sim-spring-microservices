package com.banksim.auth_service.service;

import com.banksim.auth_service.dto.GeneratedToken;
import com.banksim.auth_service.dto.request.LoginRequest;
import com.banksim.auth_service.dto.request.RegisterRequest;
import com.banksim.auth_service.dto.response.CreateUserResponse;
import com.banksim.auth_service.entity.User;
import com.banksim.auth_service.repository.UserProfileRepository;
import com.banksim.auth_service.repository.UserRepository;
import com.banksim.auth_service.security.role.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private AccountProvisioningClient accountProvisioningClient;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUserShouldRejectDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("luan", "Pass@123", "Luan Silva", "luan@example.com", "Sao Paulo");
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.registerUser(request));

        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void registerUserShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("luan", "Pass@123", "Luan Silva", "luan@example.com", "Sao Paulo");
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userProfileRepository.existsByEmail(request.email())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.registerUser(request));

        assertEquals("Email already exists", ex.getMessage());
    }

    @Test
    void registerUserShouldPersistUserAndProvisionLinkedAccount() {
        RegisterRequest request = new RegisterRequest("luan", "Pass@123", "Luan Silva", "luan@example.com", "Sao Paulo");
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userProfileRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });
        when(accountProvisioningClient.createAccount(userId, request.fullName(), request.email())).thenReturn(accountId);

        CreateUserResponse response = authService.registerUser(request);

        assertEquals(userId, response.id());
        assertEquals(accountId, response.accountId());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertEquals(request.username(), savedUser.getUsername());
        assertEquals("hashed-password", savedUser.getPassword());
        assertTrue(savedUser.isEnabled());
        assertTrue(savedUser.getRoles().contains(Role.USER));
        assertEquals(request.fullName(), savedUser.getProfile().getFullName());
        assertEquals(request.email(), savedUser.getProfile().getEmail());
        assertEquals(savedUser, savedUser.getProfile().getUser());

        verify(accountProvisioningClient).createAccount(userId, request.fullName(), request.email());
    }

    @Test
    void loginShouldAuthenticateAndReturnGeneratedToken() {
        LoginRequest request = new LoginRequest("luan", "Pass@123");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("luan");
        user.setPassword("encoded");
        user.setEnabled(true);

        GeneratedToken expected = new GeneratedToken("token-value", "Bearer", 1800L);

        when(userRepository.findByUsername("luan")).thenReturn(Optional.of(user));
        when(tokenService.generate(user)).thenReturn(expected);

        GeneratedToken result = authService.login(request);

        assertEquals(expected, result);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("luan", captor.getValue().getPrincipal());
        assertEquals("Pass@123", captor.getValue().getCredentials());
    }

    @Test
    void logoutShouldBlacklistToken() {
        Jwt jwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "none")
                .claim("sub", "user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        authService.logout(jwt);

        verify(tokenService).blacklist(jwt);
    }
}
