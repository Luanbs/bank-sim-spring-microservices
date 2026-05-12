package com.banksim.account_service.repository;

import com.banksim.account_service.entity.AccountCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountCardRepository extends JpaRepository<AccountCard, UUID> {
    List<AccountCard> findByAccountIdOrderByBrandAsc(UUID accountId);
}
