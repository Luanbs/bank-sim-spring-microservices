package com.banksim.account_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "accounts")
public class Account extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_name", nullable = false, length = 120)
    private String ownerName;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "email_key", length = 255)
    private String emailKey;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    public Account(String ownerName, BigDecimal balance) {
        this.ownerName = ownerName;
        this.balance = balance == null ? BigDecimal.ZERO : balance;
    }
}
