package com.banksim.account_service.controller;

import com.banksim.account_service.dto.request.InternalCreateAccountRequest;
import com.banksim.account_service.dto.request.TransferRequest;
import com.banksim.account_service.dto.response.AccountResponse;
import com.banksim.account_service.dto.response.ContactResponse;
import com.banksim.account_service.dto.response.TransferResponse;
import com.banksim.account_service.dto.response.dashboard.AccountDashboardResponse;
import com.banksim.account_service.dto.response.dashboard.CardsResponse;
import com.banksim.account_service.dto.response.dashboard.RecentTransactionsResponse;
import com.banksim.account_service.dto.response.dashboard.SavingsGoalsResponse;
import com.banksim.account_service.dto.response.dashboard.SpendingOverviewResponse;
import com.banksim.account_service.dto.response.dashboard.UpcomingBillsResponse;
import com.banksim.account_service.service.AccountService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    public ResponseEntity<AccountDashboardResponse> getById(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.getDashboard(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/spending-overview")
    public ResponseEntity<SpendingOverviewResponse> getSpendingOverview(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.getSpendingOverview(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/transactions/recent")
    public ResponseEntity<RecentTransactionsResponse> getRecentTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limit) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.getRecentTransactions(userId, limit));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/bills/upcoming")
    public ResponseEntity<UpcomingBillsResponse> getUpcomingBills(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.getUpcomingBills(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cards")
    public ResponseEntity<CardsResponse> getCards(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.getCards(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/savings-goals")
    public ResponseEntity<SavingsGoalsResponse> getSavingsGoals(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(accountService.getSavingsGoals(userId));
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
