package com.banksim.account_service.dto.response.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpcomingBillItemResponse(
        UUID id,
        String name,
        String category,
        BigDecimal amount,
        LocalDate dueDate
) {
}
