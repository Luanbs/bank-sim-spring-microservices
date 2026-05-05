package com.banksim.account_service.controller;

import com.banksim.account_service.dto.request.InternalCreateAccountRequest;
import com.banksim.account_service.dto.request.TransferRequest;
import com.banksim.account_service.dto.response.AccountResponse;
import com.banksim.account_service.dto.response.ContactResponse;
import com.banksim.account_service.dto.response.TransferResponse;
import com.banksim.account_service.service.AccountService;
import jakarta.validation.constraints.Email;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping()
@Validated
public class AccountController {

    private final AccountService accountService;

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasRole('SERVICE')")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody InternalCreateAccountRequest request) {

        AccountResponse response = accountService.createAccount(
                request.userId(),
                request.ownerName(),
                request.email());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getById(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.getAccountFromUserId(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/contacts/recent")
    public ResponseEntity<List<ContactResponse>> getRecentContacts(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.getRecentContacts(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{email:.+}")
    public ResponseEntity<ContactResponse> verifyContact(@PathVariable @Email String email) {
        return ResponseEntity.ok(accountService.verifyContact(email));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody TransferRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.transfer(userId, request));
    }
}
