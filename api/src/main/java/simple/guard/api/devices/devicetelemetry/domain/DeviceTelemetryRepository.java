package simple.guard.api.devices.devicetelemetry.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTelemetryRepository extends JpaRepository<DeviceTelemetry, UUID> {}
