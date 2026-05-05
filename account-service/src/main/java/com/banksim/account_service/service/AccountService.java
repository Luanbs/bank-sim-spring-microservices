package com.banksim.account_service.service;

import com.banksim.account_service.dto.request.BalanceChangeRequest;
import com.banksim.account_service.dto.request.TransferRequest;
import com.banksim.account_service.dto.request.UpdateAccountRequest;
import com.banksim.account_service.dto.response.AccountResponse;
import com.banksim.account_service.dto.response.ContactResponse;
import com.banksim.account_service.dto.response.TransferResponse;
import com.banksim.account_service.entity.Account;
import com.banksim.account_service.entity.AccountTransfer;
import com.banksim.account_service.repository.AccountRepository;
import com.banksim.account_service.repository.AccountTransferRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransferRepository accountTransferRepository;

    public AccountService(AccountRepository accountRepository, AccountTransferRepository accountTransferRepository) {
        this.accountRepository = accountRepository;
        this.accountTransferRepository = accountTransferRepository;
    }

    @Transactional
    public AccountResponse createAccount(UUID userId, String ownerName, String email) {
        if (accountRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this user.");
        }

        String normalizedEmail = normalizeEmail(email);
        if (accountRepository.findByEmailKey(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this email key.");
        }

        var newAccount = new Account();
        newAccount.setOwnerName(ownerName);
        newAccount.setUserId(userId);
        newAccount.setEmailKey(normalizedEmail);
        //$100 Balance for debugging:
        newAccount.setBalance(BigDecimal.valueOf(100.00));

        accountRepository.save(newAccount);

        return toResponse(newAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountFromUserId(UUID id) {
        return toResponse(findAccountByUserId(id));
    }

    @Transactional(readOnly = true)
    public ContactResponse verifyContact(String email) {
        Account account = findAccountByEmailKey(normalizeEmail(email));
        return toContactResponse(account);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getRecentContacts(UUID userId) {
        Account account = findAccountByUserId(userId);
        return accountTransferRepository.findRecentContacts(account.getId()).stream()
                .map(contact -> new ContactResponse(contact.getName(), contact.getEmail()))
                .toList();
    }

    @Transactional
    public TransferResponse transfer(UUID userId, TransferRequest request) {
        Account senderAccount = findAccountByUserId(userId);
        Account recipientAccount = findAccountByEmailKey(normalizeEmail(request.recipientEmail()));

        if (senderAccount.getId().equals(recipientAccount.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to your own account.");
        }

        Account lockedSender;
        Account lockedRecipient;

        if (senderAccount.getId().compareTo(recipientAccount.getId()) <= 0) {
            lockedSender = accountRepository.findByIdForUpdate(senderAccount.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
            lockedRecipient = accountRepository.findByIdForUpdate(recipientAccount.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
        } else {
            lockedRecipient = accountRepository.findByIdForUpdate(recipientAccount.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
            lockedSender = accountRepository.findByIdForUpdate(senderAccount.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
        }

        BigDecimal newBalance = lockedSender.getBalance().subtract(request.amount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance.");
        }

        lockedSender.setBalance(newBalance);
        lockedRecipient.setBalance(lockedRecipient.getBalance().add(request.amount()));

        accountRepository.save(lockedSender);
        accountRepository.save(lockedRecipient);

        AccountTransfer transfer = new AccountTransfer();
        transfer.setSenderAccount(lockedSender);
        transfer.setRecipientAccount(lockedRecipient);
        transfer.setAmount(request.amount());
        transfer.setTransferredAt(Instant.now());

        accountTransferRepository.save(transfer);

        return new TransferResponse(
                transfer.getId(),
                lockedRecipient.getOwnerName(),
                lockedRecipient.getEmailKey(),
                request.amount(),
                lockedSender.getBalance(),
                transfer.getTransferredAt()
        );
    }

    @Transactional
    public AccountResponse updateAccount(UUID id, UpdateAccountRequest updateAccountRequest) {
        var account = findAccountByUserId(id);
        account.setOwnerName(updateAccountRequest.ownerName());
        accountRepository.save(account);
        return toResponse(account);
    }

    @Transactional
    public AccountResponse deposit(UUID id, BalanceChangeRequest request) {
        var account = findAccountByUserId(id);
        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);
        return toResponse(account);
    }

    @Transactional
    public AccountResponse withdraw(UUID id, BalanceChangeRequest request) {
        var account = findAccountByUserId(id);
        BigDecimal newBalance = account.getBalance().subtract(request.amount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance.");
        }
        account.setBalance(newBalance);
        accountRepository.save(account);
        return toResponse(account);
    }

    private Account findAccountByUserId(UUID id) {
        return accountRepository.findAccountByUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
    }

    private Account findAccountByEmailKey(String emailKey) {
        return accountRepository.findByEmailKey(emailKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
    }

    private ContactResponse toContactResponse(Account account) {
        return new ContactResponse(account.getOwnerName(), account.getEmailKey());
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getOwnerName(),
                account.getBalance(),
                account.getCreatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
