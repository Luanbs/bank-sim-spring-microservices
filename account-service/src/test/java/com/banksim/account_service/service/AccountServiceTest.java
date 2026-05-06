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
import com.banksim.account_service.repository.RecentContactView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTransferRepository accountTransferRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountShouldThrowConflictWhenUserAlreadyHasAccount() {
        UUID userId = UUID.randomUUID();
        when(accountRepository.existsByUserId(userId)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.createAccount(userId, "User Name", "email@example.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createAccountShouldThrowConflictWhenEmailKeyAlreadyExists() {
        UUID userId = UUID.randomUUID();
        when(accountRepository.existsByUserId(userId)).thenReturn(false);
        when(accountRepository.findByEmailKey("email@example.com")).thenReturn(Optional.of(new Account()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.createAccount(userId, "User Name", "email@example.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createAccountShouldNormalizeEmailAndSetDefaultBalance() {
        UUID userId = UUID.randomUUID();
        when(accountRepository.existsByUserId(userId)).thenReturn(false);
        when(accountRepository.findByEmailKey("email@example.com")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(UUID.randomUUID());
            account.setCreatedAt(Instant.parse("2026-05-05T12:00:00Z"));
            return account;
        });

        AccountResponse response = accountService.createAccount(userId, "User Name", "  EMAIL@EXAMPLE.COM  ");

        assertEquals(userId, response.userId());
        assertEquals("User Name", response.ownerName());
        assertEquals(0, response.balance().compareTo(BigDecimal.valueOf(100.00)));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals("email@example.com", captor.getValue().getEmailKey());
    }

    @Test
    void transferShouldRejectSelfTransfer() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account sender = buildAccount(accountId, userId, "Sender", "sender@example.com", "200.00");
        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByEmailKey("sender@example.com")).thenReturn(Optional.of(sender));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.transfer(userId, new TransferRequest("sender@example.com", BigDecimal.TEN)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountTransferRepository, never()).save(any(AccountTransfer.class));
    }

    @Test
    void transferShouldRejectWhenBalanceIsInsufficient() {
        UUID userId = UUID.randomUUID();
        Account sender = buildAccount(UUID.fromString("00000000-0000-0000-0000-000000000001"), userId, "Sender", "sender@example.com", "10.00");
        Account recipient = buildAccount(UUID.fromString("00000000-0000-0000-0000-000000000002"), UUID.randomUUID(), "Recipient", "recipient@example.com", "20.00");

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByEmailKey("recipient@example.com")).thenReturn(Optional.of(recipient));
        when(accountRepository.findByIdForUpdate(sender.getId())).thenReturn(Optional.of(sender));
        when(accountRepository.findByIdForUpdate(recipient.getId())).thenReturn(Optional.of(recipient));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.transfer(userId, new TransferRequest("recipient@example.com", BigDecimal.valueOf(50))));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountTransferRepository, never()).save(any(AccountTransfer.class));
    }

    @Test
    void transferShouldMoveBalanceAndPersistTransfer() {
        UUID userId = UUID.randomUUID();
        Account sender = buildAccount(UUID.fromString("00000000-0000-0000-0000-000000000001"), userId, "Sender", "sender@example.com", "100.00");
        Account recipient = buildAccount(UUID.fromString("00000000-0000-0000-0000-000000000002"), UUID.randomUUID(), "Recipient", "recipient@example.com", "20.00");

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByEmailKey("recipient@example.com")).thenReturn(Optional.of(recipient));
        when(accountRepository.findByIdForUpdate(sender.getId())).thenReturn(Optional.of(sender));
        when(accountRepository.findByIdForUpdate(recipient.getId())).thenReturn(Optional.of(recipient));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountTransferRepository.save(any(AccountTransfer.class))).thenAnswer(invocation -> {
            AccountTransfer transfer = invocation.getArgument(0);
            transfer.setId(UUID.fromString("00000000-0000-0000-0000-000000000099"));
            return transfer;
        });

        TransferResponse response = accountService.transfer(userId, new TransferRequest("recipient@example.com", BigDecimal.valueOf(25)));

        assertEquals("Recipient", response.recipientName());
        assertEquals("recipient@example.com", response.recipientEmail());
        assertTrue(response.currentBalance().compareTo(BigDecimal.valueOf(75)) == 0);
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000099"), response.transferId());
        verify(accountRepository).save(sender);
        verify(accountRepository).save(recipient);
        verify(accountTransferRepository).save(any(AccountTransfer.class));
    }

    @Test
    void getRecentContactsShouldMapProjectionToResponse() {
        UUID userId = UUID.randomUUID();
        Account sender = buildAccount(UUID.randomUUID(), userId, "Sender", "sender@example.com", "100.00");

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(sender));
        when(accountTransferRepository.findRecentContacts(sender.getId())).thenReturn(List.of(
                projection("Alice", "alice@example.com"),
                projection("Bob", "bob@example.com")
        ));

        List<ContactResponse> contacts = accountService.getRecentContacts(userId);

        assertEquals(2, contacts.size());
        assertEquals("Alice", contacts.get(0).name());
        assertEquals("bob@example.com", contacts.get(1).email());
    }

    @Test
    void updateDepositAndWithdrawShouldMutateBalanceAndOwner() {
        UUID userId = UUID.randomUUID();
        Account account = buildAccount(UUID.randomUUID(), userId, "Old Name", "old@example.com", "100.00");

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse updated = accountService.updateAccount(userId, new UpdateAccountRequest("New Name"));
        AccountResponse deposited = accountService.deposit(userId, new BalanceChangeRequest(BigDecimal.valueOf(50)));
        AccountResponse withdrawn = accountService.withdraw(userId, new BalanceChangeRequest(BigDecimal.valueOf(20)));

        assertEquals("New Name", updated.ownerName());
        assertTrue(deposited.balance().compareTo(BigDecimal.valueOf(150)) == 0);
        assertTrue(withdrawn.balance().compareTo(BigDecimal.valueOf(130)) == 0);
    }

    private static Account buildAccount(UUID id, UUID userId, String ownerName, String emailKey, String balance) {
        Account account = new Account();
        account.setId(id);
        account.setUserId(userId);
        account.setOwnerName(ownerName);
        account.setEmailKey(emailKey);
        account.setBalance(new BigDecimal(balance));
        return account;
    }

    private static RecentContactView projection(String name, String email) {
        return new RecentContactView() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getEmail() {
                return email;
            }
        };
    }
}
