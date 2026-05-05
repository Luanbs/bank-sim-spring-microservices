package com.banksim.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank @Size(min = 4, max = 50) String username,

        @NotBlank @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&^()\\-_=+]).{8,}$", message = "Password must be at least 8 characters long and include at least one letter, one number, and one special character.") String password,

        @NotBlank @Size(min = 3, max = 100) String fullName,

        @NotBlank @Email(message = "Email must be valid") String email,

        @NotBlank @Size(min = 3,max = 100, message = "Location is too long") String location

) {
}