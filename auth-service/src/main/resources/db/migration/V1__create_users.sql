create table users (
                    id uuid primary key,
                    username varchar(120) not null unique,
                    password_hash varchar(255) not null,
                    enabled boolean not null default true,
                    locked_until timestamptz,
                    created_at timestamptz not null default now(),
                    updated_at timestamptz not null default now(),
                    version bigint NOT NULL default 0
);

create table user_roles (
                            user_id uuid not null references users(id) on delete cascade,
                            role varchar(50) not null,
                            primary key (user_id, role)
);
