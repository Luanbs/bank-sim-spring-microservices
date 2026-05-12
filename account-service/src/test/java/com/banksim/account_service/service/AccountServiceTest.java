package com.banksim.account_service.service;

import com.banksim.account_service.dto.request.BalanceChangeRequest;
import com.banksim.account_service.dto.request.TransferRequest;
import com.banksim.account_service.dto.request.UpdateAccountRequest;
import com.banksim.account_service.dto.response.AccountResponse;
import com.banksim.account_service.dto.response.ContactResponse;
import com.banksim.account_service.dto.response.TransferResponse;
import com.banksim.account_service.dto.response.dashboard.AccountDashboardResponse;
import com.banksim.account_service.dto.response.dashboard.CardsResponse;
import com.banksim.account_service.dto.response.dashboard.RecentTransactionsResponse;
import com.banksim.account_service.dto.response.dashboard.SavingsGoalsResponse;
import com.banksim.account_service.dto.response.dashboard.SpendingOverviewResponse;
import com.banksim.account_service.dto.response.dashboard.UpcomingBillsResponse;
import com.banksim.account_service.entity.Account;
import com.banksim.account_service.entity.AccountBill;
import com.banksim.account_service.entity.AccountCard;
import com.banksim.account_service.entity.AccountHistory;
import com.banksim.account_service.entity.AccountHistoryEntryType;
import com.banksim.account_service.entity.AccountHistoryFlowType;
import com.banksim.account_service.entity.AccountTransfer;
import com.banksim.account_service.entity.SavingsGoal;
import com.banksim.account_service.repository.AccountBillRepository;
import com.banksim.account_service.repository.AccountCardRepository;
import com.banksim.account_service.repository.AccountHistoryRepository;
import com.banksim.account_service.repository.AccountRepository;
import com.banksim.account_service.repository.AccountTransferRepository;
import com.banksim.account_service.repository.DailySpendingView;
import com.banksim.account_service.repository.RecentContactView;
import com.banksim.account_service.repository.SavingsGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTransferRepository accountTransferRepository;

    @Mock
    private AccountHistoryRepository accountHistoryRepository;

    @Mock
    private AccountBillRepository accountBillRepository;

    @Mock
    private AccountCardRepository accountCardRepository;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

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
        ArgumentCaptor<AccountHistory> historyCaptor = ArgumentCaptor.forClass(AccountHistory.class);
        verify(accountHistoryRepository).save(historyCaptor.capture());
        assertEquals(AccountHistoryEntryType.INITIAL_BALANCE, historyCaptor.getValue().getEntryType());
        assertEquals(AccountHistoryFlowType.INCOME, historyCaptor.getValue().getFlowType());
        assertEquals("Initial balance", historyCaptor.getValue().getTitle());
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
        verify(accountHistoryRepository, never()).save(any(AccountHistory.class));
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
        verify(accountHistoryRepository, never()).save(any(AccountHistory.class));
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
        ArgumentCaptor<AccountHistory> historyCaptor = ArgumentCaptor.forClass(AccountHistory.class);
        verify(accountHistoryRepository, times(2)).save(historyCaptor.capture());
        List<AccountHistory> historyEntries = historyCaptor.getAllValues();
        List<AccountHistoryEntryType> entryTypes = historyEntries.stream().map(AccountHistory::getEntryType).toList();
        assertTrue(entryTypes.contains(AccountHistoryEntryType.TRANSFER_OUT));
        assertTrue(entryTypes.contains(AccountHistoryEntryType.TRANSFER_IN));
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
        verify(accountHistoryRepository, times(2)).save(any(AccountHistory.class));
    }

    @Test
    void getRecentTransactionsShouldReturnMappedHistory() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account account = buildAccount(accountId, userId, "Sender", "sender@example.com", "100.00");

        AccountHistory income = buildHistory(
                UUID.randomUUID(),
                account,
                "Transfer from Alice",
                "Transfer",
                "50.00",
                AccountHistoryFlowType.INCOME,
                AccountHistoryEntryType.TRANSFER_IN,
                Instant.parse("2026-05-10T10:15:30Z")
        );
        AccountHistory expense = buildHistory(
                UUID.randomUUID(),
                account,
                "Coffee",
                "Food & Drink",
                "7.50",
                AccountHistoryFlowType.EXPENSE,
                AccountHistoryEntryType.WITHDRAWAL,
                Instant.parse("2026-05-09T08:00:00Z")
        );

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(account));
        when(accountHistoryRepository.findByAccountIdOrderByOccurredAtDesc(org.mockito.ArgumentMatchers.eq(accountId), any(Pageable.class)))
                .thenReturn(List.of(income, expense));

        RecentTransactionsResponse response = accountService.getRecentTransactions(userId, 5);

        assertEquals(2, response.transactions().size());
        assertEquals("income", response.transactions().get(0).type());
        assertEquals("expense", response.transactions().get(1).type());
        assertEquals("Coffee", response.transactions().get(1).title());
    }

    @Test
    void getRecentTransactionsShouldNormalizeLimitToOneWhenZeroIsProvided() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account account = buildAccount(accountId, userId, "Sender", "sender@example.com", "100.00");

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(account));
        when(accountHistoryRepository.findByAccountIdOrderByOccurredAtDesc(eq(accountId), any(Pageable.class)))
                .thenReturn(List.of());

        accountService.getRecentTransactions(userId, 0);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(accountHistoryRepository).findByAccountIdOrderByOccurredAtDesc(eq(accountId), pageableCaptor.capture());
        assertEquals(1, pageableCaptor.getValue().getPageSize());
        assertEquals(PageRequest.of(0, 1), pageableCaptor.getValue());
    }

    @Test
    void getSpendingOverviewShouldAggregateDailySpend() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account account = buildAccount(accountId, userId, "Sender", "sender@example.com", "100.00");

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(account));
        when(accountHistoryRepository.findDailySpendingOverview(accountId))
                .thenReturn(List.of(
                        dailySpending(LocalDate.parse("2026-05-05"), new BigDecimal("20.00")),
                        dailySpending(LocalDate.parse("2026-05-06"), new BigDecimal("10.50"))
                ));

        SpendingOverviewResponse response = accountService.getSpendingOverview(userId);

        assertEquals(2, response.data().size());
        assertEquals("20.00", response.data().get(0).spent().toPlainString());
        assertEquals(LocalDate.parse("2026-05-06"), response.data().get(1).date());
    }

    @Test
    void getDashboardShouldConsolidateAllSources() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account account = buildAccount(accountId, userId, "Sender", "sender@example.com", "100.00");

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(account));
        when(accountHistoryRepository.findDailySpendingOverview(accountId))
                .thenReturn(List.of(dailySpending(LocalDate.parse("2026-05-07"), new BigDecimal("12.34"))));
        when(accountHistoryRepository.findByAccountIdOrderByOccurredAtDesc(org.mockito.ArgumentMatchers.eq(accountId), any(Pageable.class)))
                .thenReturn(List.of(buildHistory(
                        UUID.randomUUID(),
                        account,
                        "Deposit",
                        "Deposit",
                        "40.00",
                        AccountHistoryFlowType.INCOME,
                        AccountHistoryEntryType.DEPOSIT,
                        Instant.parse("2026-05-08T12:00:00Z")
                )));
        when(accountBillRepository.findByAccountIdAndDueDateGreaterThanEqualOrderByDueDateAsc(org.mockito.ArgumentMatchers.eq(accountId), any(LocalDate.class)))
                .thenReturn(List.of());
        when(accountCardRepository.findByAccountIdOrderByBrandAsc(accountId)).thenReturn(List.of());
        when(savingsGoalRepository.findByAccountIdOrderByNameAsc(accountId)).thenReturn(List.of());

        AccountDashboardResponse response = accountService.getDashboard(userId);

        assertEquals(accountId, response.id());
        assertEquals(1, response.spendingOverview().size());
        assertEquals(1, response.recentTransactions().size());
        assertEquals(0, response.upcomingBills().size());
    }

    @Test
    void getUpcomingBillsShouldMapRepositoryPayload() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account account = buildAccount(accountId, userId, "Sender", "sender@example.com", "100.00");
        AccountBill firstBill = buildBill(UUID.randomUUID(), account, "Internet", "Utilities", "95.20", LocalDate.parse("2026-05-15"));
        AccountBill secondBill = buildBill(UUID.randomUUID(), account, "Streaming", "Subscription", "39.90", LocalDate.parse("2026-05-20"));

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(account));
        when(accountBillRepository.findByAccountIdAndDueDateGreaterThanEqualOrderByDueDateAsc(eq(accountId), any(LocalDate.class)))
                .thenReturn(List.of(firstBill, secondBill));

        UpcomingBillsResponse response = accountService.getUpcomingBills(userId);

        assertEquals(2, response.bills().size());
        assertEquals("Internet", response.bills().get(0).name());
        assertEquals(LocalDate.parse("2026-05-20"), response.bills().get(1).dueDate());
    }

    @Test
    void getCardsShouldMapRepositoryPayload() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account account = buildAccount(accountId, userId, "Sender", "sender@example.com", "100.00");
        AccountCard visa = buildCard(UUID.randomUUID(), account, "Credit", "1234", "12/29", "VISA");
        AccountCard master = buildCard(UUID.randomUUID(), account, "Debit", "9876", "11/28", "MASTERCARD");

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(account));
        when(accountCardRepository.findByAccountIdOrderByBrandAsc(accountId)).thenReturn(List.of(visa, master));

        CardsResponse response = accountService.getCards(userId);

        assertEquals(2, response.cards().size());
        assertEquals("VISA", response.cards().get(0).brand());
        assertEquals("9876", response.cards().get(1).last4());
    }

    @Test
    void getSavingsGoalsShouldMapRepositoryPayload() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account account = buildAccount(accountId, userId, "Sender", "sender@example.com", "100.00");
        SavingsGoal emergency = buildSavingsGoal(UUID.randomUUID(), account, "Emergency Fund", "350.00", "5000.00");
        SavingsGoal vacation = buildSavingsGoal(UUID.randomUUID(), account, "Vacation", "1200.00", "8000.00");

        when(accountRepository.findAccountByUserId(userId)).thenReturn(Optional.of(account));
        when(savingsGoalRepository.findByAccountIdOrderByNameAsc(accountId)).thenReturn(List.of(emergency, vacation));

        SavingsGoalsResponse response = accountService.getSavingsGoals(userId);

        assertEquals(2, response.goals().size());
        assertEquals("Emergency Fund", response.goals().get(0).name());
        assertEquals(new BigDecimal("8000.00"), response.goals().get(1).targetAmount());
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

    private static AccountHistory buildHistory(UUID id,
                                               Account account,
                                               String title,
                                               String category,
                                               String amount,
                                               AccountHistoryFlowType flowType,
                                               AccountHistoryEntryType entryType,
                                               Instant occurredAt) {
        AccountHistory history = new AccountHistory();
        history.setId(id);
        history.setAccount(account);
        history.setTitle(title);
        history.setCategory(category);
        history.setAmount(new BigDecimal(amount));
        history.setFlowType(flowType);
        history.setEntryType(entryType);
        history.setOccurredAt(occurredAt);
        return history;
    }

    private static DailySpendingView dailySpending(LocalDate date, BigDecimal spent) {
        return new DailySpendingView() {
            @Override
            public LocalDate getDate() {
                return date;
            }

            @Override
            public BigDecimal getSpent() {
                return spent;
            }
        };
    }

    private static AccountBill buildBill(UUID id,
                                         Account account,
                                         String name,
                                         String category,
                                         String amount,
                                         LocalDate dueDate) {
        AccountBill bill = new AccountBill();
        bill.setId(id);
        bill.setAccount(account);
        bill.setName(name);
        bill.setCategory(category);
        bill.setAmount(new BigDecimal(amount));
        bill.setDueDate(dueDate);
        return bill;
    }

    private static AccountCard buildCard(UUID id,
                                         Account account,
                                         String type,
                                         String last4,
                                         String expiry,
                                         String brand) {
        AccountCard card = new AccountCard();
        card.setId(id);
        card.setAccount(account);
        card.setType(type);
        card.setLast4(last4);
        card.setExpiry(expiry);
        card.setBrand(brand);
        return card;
    }

    private static SavingsGoal buildSavingsGoal(UUID id,
                                                Account account,
                                                String name,
                                                String currentAmount,
                                                String targetAmount) {
        SavingsGoal goal = new SavingsGoal();
        goal.setId(id);
        goal.setAccount(account);
        goal.setName(name);
        goal.setCurrentAmount(new BigDecimal(currentAmount));
        goal.setTargetAmount(new BigDecimal(targetAmount));
        return goal;
    }
}
