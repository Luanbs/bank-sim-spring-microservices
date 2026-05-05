package com.banksim.auth_service.service;

import com.banksim.auth_service.dto.request.CreateLinkedAccountRequest;
import com.banksim.auth_service.dto.response.LinkedAccountResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AccountProvisioningClient {

    private final RestClient restClient;
    private final TokenService tokenService;

    public AccountProvisioningClient(
            RestClient.Builder restClientBuilder,
            @Value("${account-service.base-url}") String accountServiceBaseUrl, TokenService tokenService) {
        this.tokenService = tokenService;
        this.restClient = restClientBuilder.baseUrl(accountServiceBaseUrl).build();
    }

    public UUID createAccount(UUID userId, String ownerName, String email) {
        try {
            String generatedServiceToken = tokenService.generateServiceToken();
            LinkedAccountResponse response = restClient.post()
                    .header("Authorization", "Bearer " + generatedServiceToken)
                    .body(new CreateLinkedAccountRequest(userId, ownerName, email))
                    .retrieve()
                    .body(LinkedAccountResponse.class);

            if (response == null || response.id() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Account service returned an invalid response.");
            }

            return response.id();
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Account service failed to create a linked account.",
                    ex
            );
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Account service is unavailable.",
                    ex
            );
        }
    }
}
