create table pairing_sessions (
    id uuid primary key,
    device_id uuid not null references devices(id) on delete cascade,
    account_id uuid not null references accounts(id),
    code_hash char(64) not null unique,
    status varchar(32) not null,
    expiration_reason varchar(32),
    expires_at timestamp with time zone not null,
    used_at timestamp with time zone,
    expired_at timestamp with time zone,
    created_by varchar(128) not null,
    created_at timestamp with time zone not null,
    updated_by varchar(128) not null,
    updated_at timestamp with time zone not null,
    version bigint not null default 0
);

create index idx_pairing_sessions_device_status
    on pairing_sessions (device_id, status);

create index idx_pairing_sessions_status_expiration
    on pairing_sessions (status, expires_at);

create unique index uq_pairing_sessions_waiting_device
    on pairing_sessions (device_id)
    where status = 'WAITING';
