package com.banksim.account_service.dto.response.dashboard;

import java.util.List;

public record RecentTransactionsResponse(List<TransactionItemResponse> transactions) {
}
