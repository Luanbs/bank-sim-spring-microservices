CREATE TABLE accounts (
                        id UUID PRIMARY KEY,
                        owner_name VARCHAR(120) NOT NULL,
                        balance NUMERIC(19,2) NOT NULL DEFAULT 0,
                        user_id UUID NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                        version BIGINT NOT NULL DEFAULT 0
);
