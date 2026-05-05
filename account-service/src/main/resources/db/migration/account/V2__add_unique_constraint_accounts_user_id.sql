alter table accounts
    add constraint uk_accounts_user_id unique (user_id);
