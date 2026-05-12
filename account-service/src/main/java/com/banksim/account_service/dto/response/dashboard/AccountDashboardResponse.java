package com.banksim.account_service.dto.response.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AccountDashboardResponse(
        UUID id,
        UUID userId,
        String ownerName,
        BigDecimal balance,
        List<SpendingOverviewItemResponse> spendingOverview,
        List<TransactionItemResponse> recentTransactions,
        List<UpcomingBillItemResponse> upcomingBills,
        List<CardItemResponse> cards,
        List<SavingsGoalItemResponse> savingsGoals
) {
}
