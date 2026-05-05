package com.banksim.account_service.repository;

import com.banksim.account_service.entity.AccountTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AccountTransferRepository extends JpaRepository<AccountTransfer, UUID> {

    @Query(value = """
            SELECT a.owner_name AS name, a.email_key AS email
            FROM accounts a
            JOIN (
                SELECT t.recipient_account_id, MAX(t.transferred_at) AS last_transfer
                FROM account_transfers t
                WHERE t.sender_account_id = :senderAccountId
                GROUP BY t.recipient_account_id
                ORDER BY MAX(t.transferred_at) DESC
                LIMIT 4
            ) recent ON recent.recipient_account_id = a.id
            ORDER BY recent.last_transfer DESC
            """, nativeQuery = true)
    List<RecentContactView> findRecentContacts(UUID senderAccountId);
}
