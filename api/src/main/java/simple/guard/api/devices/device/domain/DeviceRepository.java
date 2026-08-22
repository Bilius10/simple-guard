package simple.guard.api.devices.device.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

  List<Device> findAllByAccountIdOrderByCreatedAtDesc(UUID accountId);

  List<Device> findAllByAccountIdAndPairingStatusOrderByCreatedAtDesc(
      UUID accountId, DevicePairingStatus pairingStatus);

  Optional<Device> findByIdAndAccountId(UUID id, UUID accountId);
}
