package simple.guard.api.devices.devicelocation.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceLocationRepository extends JpaRepository<DeviceLocation, UUID> {
}
