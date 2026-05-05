alter table accounts
    add column email_key varchar(255);

create unique index if not exists uk_accounts_email_key
    on accounts (email_key)
    where email_key is not null;
