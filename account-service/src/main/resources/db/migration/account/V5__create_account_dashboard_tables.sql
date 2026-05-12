create table account_history (
    id uuid primary key,
    account_id uuid not null,
    title varchar(120) not null,
    category varchar(80) not null,
    amount numeric(19,2) not null,
    flow_type varchar(20) not null,
    entry_type varchar(40) not null,
    occurred_at timestamptz not null,
    reference_type varchar(50),
    reference_id uuid,
    constraint fk_account_history_account
        foreign key (account_id) references accounts(id)
);

create index if not exists idx_account_history_account_occurred_at
    on account_history (account_id, occurred_at desc);

create index if not exists idx_account_history_account_flow_type
    on account_history (account_id, flow_type);

create table account_bills (
    id uuid primary key,
    account_id uuid not null,
    name varchar(120) not null,
    category varchar(80) not null,
    amount numeric(19,2) not null,
    due_date date not null,
    constraint fk_account_bills_account
        foreign key (account_id) references accounts(id)
);

create index if not exists idx_account_bills_account_due_date
    on account_bills (account_id, due_date);

create table account_cards (
    id uuid primary key,
    account_id uuid not null,
    type varchar(40) not null,
    last4 varchar(4) not null,
    expiry varchar(7) not null,
    brand varchar(40) not null,
    constraint fk_account_cards_account
        foreign key (account_id) references accounts(id)
);

create index if not exists idx_account_cards_account
    on account_cards (account_id);

create table account_savings_goals (
    id uuid primary key,
    account_id uuid not null,
    name varchar(120) not null,
    current_amount numeric(19,2) not null,
    target_amount numeric(19,2) not null,
    constraint fk_account_savings_goals_account
        foreign key (account_id) references accounts(id)
);

create index if not exists idx_account_savings_goals_account
    on account_savings_goals (account_id);
