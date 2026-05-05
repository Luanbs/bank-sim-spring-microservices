create table account_transfers (
    id uuid primary key,
    sender_account_id uuid not null,
    recipient_account_id uuid not null,
    amount numeric(19,2) not null,
    transferred_at timestamptz not null,
    constraint fk_account_transfers_sender
        foreign key (sender_account_id) references accounts(id),
    constraint fk_account_transfers_recipient
        foreign key (recipient_account_id) references accounts(id)
);

create index if not exists idx_account_transfers_sender_transferred_at
    on account_transfers (sender_account_id, transferred_at desc);

create index if not exists idx_account_transfers_sender_recipient
    on account_transfers (sender_account_id, recipient_account_id);
