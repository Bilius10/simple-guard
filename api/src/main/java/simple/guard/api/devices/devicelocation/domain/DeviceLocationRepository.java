package simple.guard.api.devices.devicelocation.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceLocationRepository extends JpaRepository<DeviceLocation, UUID> {

  Optional<DeviceLocation> findFirstByDeviceIdOrderByCollectedAtDesc(UUID deviceId);

  @Query(
      value =
          """
          select *
          from (
            select location.*,
                   row_number() over (
                     partition by location.device_id
                     order by location.collected_at desc, location.received_at desc, location.id desc
                   ) as location_rank
            from device_locations location
            where location.device_id in (:deviceIds)
          ) latest
          where latest.location_rank = 1
          """,
      nativeQuery = true)
  List<DeviceLocation> findLatestByDeviceIds(@Param("deviceIds") Collection<UUID> deviceIds);
}
