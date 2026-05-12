package com.banksim.account_service.service;

import com.banksim.account_service.dto.request.BalanceChangeRequest;
import com.banksim.account_service.dto.request.TransferRequest;
import com.banksim.account_service.dto.request.UpdateAccountRequest;
import com.banksim.account_service.dto.response.AccountResponse;
import com.banksim.account_service.dto.response.ContactResponse;
import com.banksim.account_service.dto.response.TransferResponse;
import com.banksim.account_service.dto.response.dashboard.AccountDashboardResponse;
import com.banksim.account_service.dto.response.dashboard.CardItemResponse;
import com.banksim.account_service.dto.response.dashboard.CardsResponse;
import com.banksim.account_service.dto.response.dashboard.RecentTransactionsResponse;
import com.banksim.account_service.dto.response.dashboard.SavingsGoalItemResponse;
import com.banksim.account_service.dto.response.dashboard.SavingsGoalsResponse;
import com.banksim.account_service.dto.response.dashboard.SpendingOverviewItemResponse;
import com.banksim.account_service.dto.response.dashboard.SpendingOverviewResponse;
import com.banksim.account_service.dto.response.dashboard.TransactionItemResponse;
import com.banksim.account_service.dto.response.dashboard.UpcomingBillItemResponse;
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
import com.banksim.account_service.repository.SavingsGoalRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AccountService {

    private static final DateTimeFormatter SPENDING_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM dd", Locale.US);

    private final AccountRepository accountRepository;
    private final AccountTransferRepository accountTransferRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    private final AccountBillRepository accountBillRepository;
    private final AccountCardRepository accountCardRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public AccountService(AccountRepository accountRepository,
                          AccountTransferRepository accountTransferRepository,
                          AccountHistoryRepository accountHistoryRepository,
                          AccountBillRepository accountBillRepository,
                          AccountCardRepository accountCardRepository,
                          SavingsGoalRepository savingsGoalRepository) {
        this.accountRepository = accountRepository;
        this.accountTransferRepository = accountTransferRepository;
        this.accountHistoryRepository = accountHistoryRepository;
        this.accountBillRepository = accountBillRepository;
        this.accountCardRepository = accountCardRepository;
        this.savingsGoalRepository = savingsGoalRepository;
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
        recordHistory(
                newAccount,
                AccountHistoryEntryType.INITIAL_BALANCE,
                AccountHistoryFlowType.INCOME,
                "Initial balance",
                "Account",
                newAccount.getBalance(),
                Instant.now(),
                null,
                null
        );

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
        recordHistory(
                lockedSender,
                AccountHistoryEntryType.TRANSFER_OUT,
                AccountHistoryFlowType.EXPENSE,
                "Transfer to " + lockedRecipient.getOwnerName(),
                "Transfer",
                request.amount(),
                transfer.getTransferredAt(),
                "ACCOUNT_TRANSFER",
                transfer.getId()
        );
        recordHistory(
                lockedRecipient,
                AccountHistoryEntryType.TRANSFER_IN,
                AccountHistoryFlowType.INCOME,
                "Transfer from " + lockedSender.getOwnerName(),
                "Transfer",
                request.amount(),
                transfer.getTransferredAt(),
                "ACCOUNT_TRANSFER",
                transfer.getId()
        );

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
        recordHistory(
                account,
                AccountHistoryEntryType.DEPOSIT,
                AccountHistoryFlowType.INCOME,
                "Deposit",
                "Deposit",
                request.amount(),
                Instant.now(),
                null,
                null
        );
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
        recordHistory(
                account,
                AccountHistoryEntryType.WITHDRAWAL,
                AccountHistoryFlowType.EXPENSE,
                "Withdrawal",
                "Withdrawal",
                request.amount(),
                Instant.now(),
                null,
                null
        );
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public SpendingOverviewResponse getSpendingOverview(UUID userId) {
        Account account = findAccountByUserId(userId);
        return buildSpendingOverviewResponse(account);
    }

    @Transactional(readOnly = true)
    public RecentTransactionsResponse getRecentTransactions(UUID userId, int limit) {
        Account account = findAccountByUserId(userId);
        return buildRecentTransactionsResponse(account, limit);
    }

    @Transactional(readOnly = true)
    public UpcomingBillsResponse getUpcomingBills(UUID userId) {
        Account account = findAccountByUserId(userId);
        return buildUpcomingBillsResponse(account);
    }

    @Transactional(readOnly = true)
    public CardsResponse getCards(UUID userId) {
        Account account = findAccountByUserId(userId);
        return buildCardsResponse(account);
    }

    @Transactional(readOnly = true)
    public SavingsGoalsResponse getSavingsGoals(UUID userId) {
        Account account = findAccountByUserId(userId);
        return buildSavingsGoalsResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountDashboardResponse getDashboard(UUID userId) {
        Account account = findAccountByUserId(userId);
        SpendingOverviewResponse spendingOverview = buildSpendingOverviewResponse(account);
        RecentTransactionsResponse recentTransactions = buildRecentTransactionsResponse(account, 5);
        UpcomingBillsResponse upcomingBills = buildUpcomingBillsResponse(account);
        CardsResponse cards = buildCardsResponse(account);
        SavingsGoalsResponse savingsGoals = buildSavingsGoalsResponse(account);

        return new AccountDashboardResponse(
                account.getId(),
                account.getUserId(),
                account.getOwnerName(),
                account.getBalance(),
                spendingOverview.data(),
                recentTransactions.transactions(),
                upcomingBills.bills(),
                cards.cards(),
                savingsGoals.goals()
        );
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

    private SpendingOverviewResponse buildSpendingOverviewResponse(Account account) {
        List<SpendingOverviewItemResponse> data = accountHistoryRepository.findDailySpendingOverview(account.getId())
                .stream()
                .map(this::toSpendingOverviewItem)
                .toList();
        return new SpendingOverviewResponse(data);
    }

    private RecentTransactionsResponse buildRecentTransactionsResponse(Account account, int limit) {
        int normalizedLimit = Math.max(1, limit);
        List<TransactionItemResponse> transactions = accountHistoryRepository
                .findByAccountIdOrderByOccurredAtDesc(account.getId(), PageRequest.of(0, normalizedLimit))
                .stream()
                .map(this::toTransactionItem)
                .toList();
        return new RecentTransactionsResponse(transactions);
    }

    private UpcomingBillsResponse buildUpcomingBillsResponse(Account account) {
        List<UpcomingBillItemResponse> bills = accountBillRepository
                .findByAccountIdAndDueDateGreaterThanEqualOrderByDueDateAsc(account.getId(), LocalDate.now())
                .stream()
                .map(this::toUpcomingBillItem)
                .toList();
        return new UpcomingBillsResponse(bills);
    }

    private CardsResponse buildCardsResponse(Account account) {
        List<CardItemResponse> cards = accountCardRepository.findByAccountIdOrderByBrandAsc(account.getId())
                .stream()
                .map(this::toCardItem)
                .toList();
        return new CardsResponse(cards);
    }

    private SavingsGoalsResponse buildSavingsGoalsResponse(Account account) {
        List<SavingsGoalItemResponse> goals = savingsGoalRepository.findByAccountIdOrderByNameAsc(account.getId())
                .stream()
                .map(this::toSavingsGoalItem)
                .toList();
        return new SavingsGoalsResponse(goals);
    }

    private SpendingOverviewItemResponse toSpendingOverviewItem(DailySpendingView spendingView) {
        return new SpendingOverviewItemResponse(
                spendingView.getDate().format(SPENDING_LABEL_FORMATTER),
                spendingView.getSpent(),
                spendingView.getDate()
        );
    }

    private TransactionItemResponse toTransactionItem(AccountHistory history) {
        String type = history.getFlowType() == AccountHistoryFlowType.INCOME ? "income" : "expense";
        return new TransactionItemResponse(
                history.getId(),
                history.getTitle(),
                history.getCategory(),
                history.getAmount(),
                history.getOccurredAt(),
                type
        );
    }

    private UpcomingBillItemResponse toUpcomingBillItem(AccountBill bill) {
        return new UpcomingBillItemResponse(
                bill.getId(),
                bill.getName(),
                bill.getCategory(),
                bill.getAmount(),
                bill.getDueDate()
        );
    }

    private CardItemResponse toCardItem(AccountCard card) {
        return new CardItemResponse(
                card.getId(),
                card.getType(),
                card.getLast4(),
                card.getExpiry(),
                card.getBrand()
        );
    }

    private SavingsGoalItemResponse toSavingsGoalItem(SavingsGoal goal) {
        return new SavingsGoalItemResponse(
                goal.getId(),
                goal.getName(),
                goal.getCurrentAmount(),
                goal.getTargetAmount()
        );
    }

    private void recordHistory(Account account,
                               AccountHistoryEntryType entryType,
                               AccountHistoryFlowType flowType,
                               String title,
                               String category,
                               BigDecimal amount,
                               Instant occurredAt,
                               String referenceType,
                               UUID referenceId) {
        AccountHistory history = new AccountHistory();
        history.setAccount(account);
        history.setEntryType(entryType);
        history.setFlowType(flowType);
        history.setTitle(title);
        history.setCategory(category);
        history.setAmount(amount);
        history.setOccurredAt(occurredAt);
        history.setReferenceType(referenceType);
        history.setReferenceId(referenceId);
        accountHistoryRepository.save(history);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
