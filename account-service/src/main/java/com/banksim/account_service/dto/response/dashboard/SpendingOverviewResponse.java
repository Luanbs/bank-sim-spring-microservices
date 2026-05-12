package com.banksim.account_service.dto.response.dashboard;

import java.util.List;

public record SpendingOverviewResponse(List<SpendingOverviewItemResponse> data) {
}
