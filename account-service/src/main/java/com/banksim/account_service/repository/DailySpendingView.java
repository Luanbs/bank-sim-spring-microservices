package com.banksim.account_service.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySpendingView {
    LocalDate getDate();
    BigDecimal getSpent();
}
