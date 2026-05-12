package com.banksim.account_service.repository;

import com.banksim.account_service.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {
    List<SavingsGoal> findByAccountIdOrderByNameAsc(UUID accountId);
}
