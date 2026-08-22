package simple.guard.api.devices.devicetelemetry.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceTelemetryRepository extends JpaRepository<DeviceTelemetry, UUID> {

  Optional<DeviceTelemetry> findFirstByDeviceIdOrderByCollectedAtDesc(UUID deviceId);

  @Query(
      value =
          """
          select *
          from (
            select telemetry.*,
                   row_number() over (
                     partition by telemetry.device_id
                     order by telemetry.collected_at desc, telemetry.received_at desc, telemetry.id desc
                   ) as telemetry_rank
            from device_telemetry telemetry
            where telemetry.device_id in (:deviceIds)
          ) latest
          where latest.telemetry_rank = 1
          """,
      nativeQuery = true)
  List<DeviceTelemetry> findLatestByDeviceIds(@Param("deviceIds") Collection<UUID> deviceIds);
}
