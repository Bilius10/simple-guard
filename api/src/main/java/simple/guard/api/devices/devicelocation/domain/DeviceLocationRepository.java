package simple.guard.api.devices.devicelocation.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceLocationRepository extends JpaRepository<DeviceLocation, UUID> {}
