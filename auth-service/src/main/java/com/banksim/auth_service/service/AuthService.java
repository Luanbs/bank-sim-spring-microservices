package com.banksim.auth_service.service;

import com.banksim.auth_service.dto.GeneratedToken;
import com.banksim.auth_service.dto.request.LoginRequest;
import com.banksim.auth_service.dto.request.RegisterRequest;
import com.banksim.auth_service.dto.response.CreateUserResponse;
import com.banksim.auth_service.entity.User;
import com.banksim.auth_service.entity.UserProfile;
import com.banksim.auth_service.repository.UserProfileRepository;
import com.banksim.auth_service.repository.UserRepository;
import com.banksim.auth_service.security.role.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AccountProvisioningClient accountProvisioningClient;

    public AuthService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService,
                       AccountProvisioningClient accountProvisioningClient) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.accountProvisioningClient = accountProvisioningClient;
    }

    @Transactional
    public CreateUserResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userProfileRepository.existsByEmail(registerRequest.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setEnabled(true);
        user.getRoles().add(Role.USER);

        UserProfile userProfile = new UserProfile();
        userProfile.setFullName(registerRequest.fullName());
        userProfile.setEmail(registerRequest.email());
        userProfile.setLocation(registerRequest.location());
        user.setProfile(userProfile);

        userRepository.save(user);

        var accountId = accountProvisioningClient.createAccount(
                user.getId(),
                registerRequest.fullName(),
                registerRequest.email());

        return new CreateUserResponse(user.getId(), accountId);
    }

    public GeneratedToken login(LoginRequest loginRequest) {
        var authToken = new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
        authenticationManager.authenticate(authToken);

        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow();

        return tokenService.generate(user);
    }

    public void logout(Jwt jwt) {
        tokenService.blacklist(jwt);
    }
}
