create table devices (
    id uuid primary key,
    account_id uuid not null references accounts(id),
    name varchar(160) not null,
    type varchar(32) not null,
    platform varchar(32) not null,
    pairing_status varchar(32) not null,
    created_by varchar(128) not null,
    created_at timestamp with time zone not null,
    updated_by varchar(128) not null,
    updated_at timestamp with time zone not null
);

create index idx_devices_account_created_at
    on devices (account_id, created_at desc);

create index idx_devices_pairing_status
    on devices (pairing_status);
