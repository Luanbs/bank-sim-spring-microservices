package com.banksim.account_service.repository;

import com.banksim.account_service.entity.AccountBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AccountBillRepository extends JpaRepository<AccountBill, UUID> {
    List<AccountBill> findByAccountIdAndDueDateGreaterThanEqualOrderByDueDateAsc(UUID accountId, LocalDate dueDate);
}
