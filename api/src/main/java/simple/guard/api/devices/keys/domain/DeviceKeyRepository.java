package simple.guard.api.devices.keys.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceKeyRepository extends JpaRepository<DeviceKey, UUID> {
}
