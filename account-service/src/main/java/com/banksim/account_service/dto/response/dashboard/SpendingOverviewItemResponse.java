package com.banksim.account_service.dto.response.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpendingOverviewItemResponse(
        String name,
        BigDecimal spent,
        LocalDate date
) {
}
