create table device_keys (
    id uuid primary key,
    device_id uuid not null references devices(id) on delete cascade,
    pairing_session_id uuid not null references pairing_sessions(id),
    agent_instance_id varchar(128) not null,
    platform varchar(32) not null,
    public_key text not null,
    status varchar(32) not null,
    created_by varchar(128) not null,
    created_at timestamp with time zone not null,
    revoked_by varchar(128),
    revoked_at timestamp with time zone,
    version bigint not null default 0
);

create unique index uq_device_keys_active_device
    on device_keys (device_id)
    where status = 'ACTIVE';

create index idx_device_keys_device_status
    on device_keys (device_id, status);
