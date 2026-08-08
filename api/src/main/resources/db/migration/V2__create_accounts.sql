create table accounts (
    id uuid primary key,
    subject varchar(128) not null unique,
    email varchar(320) not null unique,
    display_name varchar(160) not null,
    role varchar(64) not null,
    active boolean not null,
    created_by varchar(128) not null,
    created_at timestamp with time zone not null,
    updated_by varchar(128) not null,
    updated_at timestamp with time zone not null
);

insert into accounts (
    id,
    subject,
    email,
    display_name,
    role,
    active,
    created_by,
    created_at,
    updated_by,
    updated_at
)
values (
    '00000000-0000-0000-0000-000000000101',
    '00000000-0000-0000-0000-000000000001',
    'admin@simpleguard.local',
    'SimpleGuard Admin',
    'ADMIN',
    true,
    'system',
    current_timestamp,
    'system',
    current_timestamp
);
