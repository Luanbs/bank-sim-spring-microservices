package com.banksim.auth_service.security.role;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {

    USER(Set.of(
            Permission.ACCOUNT_READ,
            Permission.TRANSACTION_READ,
            Permission.TRANSACTION_CREATE
    )),

    ADMIN(Set.of(
            Permission.ACCOUNT_READ,
            Permission.ACCOUNT_CREATE,
            Permission.ACCOUNT_UPDATE,
            Permission.ACCOUNT_DELETE,
            Permission.TRANSACTION_READ,
            Permission.TRANSACTION_CREATE,
            Permission.TRANSACTION_APPROVE
    )),

    SERVICE(Set.of(
                       Permission.ACCOUNT_READ,
               Permission.ACCOUNT_CREATE,
               Permission.ACCOUNT_UPDATE,
               Permission.ACCOUNT_DELETE,
               Permission.TRANSACTION_READ,
               Permission.TRANSACTION_CREATE,
               Permission.TRANSACTION_APPROVE
               ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

}
