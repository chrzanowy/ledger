create table if not exists events (
 event_uuid UUID primary key,
 event_time TIMESTAMP WITH TIME ZONE  not null,
 event_body JSON not null
);

create table if not exists transactions (
    id UUID primary key,
    external_id UUID,
    lease_id UUID not null,
    event_id UUID not null,
    base_amount DECIMAL(19, 2) not null,
    currency VARCHAR(3) not null,
    fee_amount DECIMAL(19, 2) not null,
    created_at TIMESTAMP WITH TIME ZONE not null,
    transaction_type VARCHAR(255) not null,
    transaction_sub_type VARCHAR(255) not null,
    status VARCHAR(255) not null,
    description VARCHAR(2048)
);

create table if not exists ledgers (
    lease_id UUID not null,
    entry_date TIMESTAMP WITH TIME ZONE not null,
    calculation_date TIMESTAMP WITH TIME ZONE not null,
    description VARCHAR(2048),
    currency VARCHAR(3) not null,
    debit DECIMAL(19, 2) not null,
    credit DECIMAL(19, 2) not null,
    balance DECIMAL(19, 2) not null
);

create table if not exists scheduled_actions (
    lease_id UUID not null,
    action_date TIMESTAMP WITH TIME ZONE not null,
    action_type VARCHAR(2048)
);