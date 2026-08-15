package simple.guard.api.devices.device.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    List<Device> findAllByAccountIdOrderByCreatedAtDesc(UUID accountId);

    Optional<Device> findByIdAndAccountId(UUID id, UUID accountId);
}


