package simple.guard.api.devices.deviceunpairingrequest.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceUnpairingRequestRepository
    extends JpaRepository<DeviceUnpairingRequest, UUID> {

  List<DeviceUnpairingRequest> findAllByAccountIdAndStatusOrderByRequestedAtDesc(
      UUID accountId, DeviceUnpairingRequestStatus status);

  Optional<DeviceUnpairingRequest> findByIdAndAccountIdAndStatus(
      UUID id, UUID accountId, DeviceUnpairingRequestStatus status);

  Optional<DeviceUnpairingRequest> findFirstByDeviceIdAndStatusOrderByRequestedAtDesc(
      UUID deviceId, DeviceUnpairingRequestStatus status);

  Optional<DeviceUnpairingRequest> findFirstByDeviceIdAndAgentInstanceIdOrderByRequestedAtDesc(
      UUID deviceId, String agentInstanceId);
}
