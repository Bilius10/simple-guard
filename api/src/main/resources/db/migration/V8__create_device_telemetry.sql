create table device_telemetry (
    id uuid primary key,
    device_id uuid not null references devices(id) on delete cascade,
    battery_level_percentage smallint,
    battery_charging boolean,
    network_type varchar(16),
    signal_strength_dbm integer,
    fine_location_permission varchar(16),
    coarse_location_permission varchar(16),
    collected_at timestamp with time zone not null,
    received_at timestamp with time zone not null,
    constraint chk_device_telemetry_battery_level check (battery_level_percentage between 0 and 100),
    constraint chk_device_telemetry_network_type
        check (network_type in ('NONE', 'WIFI', 'CELLULAR', 'ETHERNET', 'VPN', 'OTHER')),
    constraint chk_device_telemetry_signal_strength check (signal_strength_dbm between -160 and 0),
    constraint chk_device_telemetry_fine_location_permission
        check (fine_location_permission in ('GRANTED', 'DENIED')),
    constraint chk_device_telemetry_coarse_location_permission
        check (coarse_location_permission in ('GRANTED', 'DENIED'))
);

create index idx_device_telemetry_device_collected_at
    on device_telemetry (device_id, collected_at desc);
