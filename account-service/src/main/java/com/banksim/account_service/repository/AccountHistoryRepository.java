package com.banksim.account_service.repository;

import com.banksim.account_service.entity.AccountHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AccountHistoryRepository extends JpaRepository<AccountHistory, UUID> {

    List<AccountHistory> findByAccountIdOrderByOccurredAtDesc(UUID accountId, Pageable pageable);

    @Query(value = """
            select
                (h.occurred_at at time zone 'UTC')::date as date,
                sum(h.amount) as spent
            from account_history h
            where h.account_id = :accountId
              and h.flow_type = 'EXPENSE'
              and h.occurred_at >= (now() - interval '30 days')
            group by (h.occurred_at at time zone 'UTC')::date
            order by date
            """, nativeQuery = true)
    List<DailySpendingView> findDailySpendingOverview(UUID accountId);
}
