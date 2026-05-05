create table user_profiles (
    user_id uuid primary key references users(id) on delete cascade,
    full_name varchar(160) not null,
    email varchar(255) not null unique,
    location varchar(255) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version bigint not null default 0
);
