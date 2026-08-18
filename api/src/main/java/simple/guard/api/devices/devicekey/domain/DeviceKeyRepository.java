package simple.guard.api.devices.devicekey.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceKeyRepository extends JpaRepository<DeviceKey, UUID> {

    List<DeviceKey> findAllByDeviceIdAndStatus(UUID deviceId, DeviceKeyStatus status);

    Optional<DeviceKey> findByDeviceIdAndAgentInstanceIdAndStatus(
            UUID deviceId,
            String agentInstanceId,
            DeviceKeyStatus status
    );

    Optional<DeviceKey> findFirstByDeviceIdAndAgentInstanceIdOrderByCreatedAtDesc(
            UUID deviceId,
            String agentInstanceId
    );
}

