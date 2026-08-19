create table device_locations (
    id uuid primary key,
    device_id uuid not null references devices(id) on delete cascade,
    position geography(Point, 4326) not null,
    accuracy_meters numeric(10, 3),
    altitude_meters numeric(12, 3),
    speed_meters_per_second numeric(10, 3),
    provider varchar(32) not null,
    collected_at timestamp with time zone not null,
    received_at timestamp with time zone not null
);

create index idx_device_locations_device_collected_at
    on device_locations (device_id, collected_at desc);

create index idx_device_locations_position
    on device_locations using gist (position);
