package simple.guard.api.devices.devicetelemetry.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceTelemetryRepository extends JpaRepository<DeviceTelemetry, UUID> {
}
