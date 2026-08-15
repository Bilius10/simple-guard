create table device_unpairing_requests (
    id uuid primary key,
    device_id uuid not null references devices(id) on delete cascade,
    account_id uuid not null references accounts(id),
    agent_instance_id varchar(128) not null,
    status varchar(32) not null,
    requested_by varchar(128) not null,
    requested_at timestamp with time zone not null,
    decided_by varchar(128),
    decided_at timestamp with time zone,
    version bigint not null default 0
);

create index idx_device_unpairing_requests_account_status
    on device_unpairing_requests (account_id, status, requested_at desc);

create unique index uq_device_unpairing_requests_pending_device
    on device_unpairing_requests (device_id)
    where status = 'PENDING';
