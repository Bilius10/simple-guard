package simple.guard.api.devices.devicelocation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryLocationRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "device_locations")
public class DeviceLocation {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    private Point position;

    @Column(precision = 10, scale = 3)
    private BigDecimal accuracyMeters;

    @Column(precision = 12, scale = 3)
    private BigDecimal altitudeMeters;

    @Column(precision = 10, scale = 3)
    private BigDecimal speedMetersPerSecond;

    @Column(nullable = false, length = 32)
    private String provider;

    @Column(nullable = false)
    private OffsetDateTime collectedAt;

    @Column(nullable = false)
    private OffsetDateTime receivedAt;

    public static DeviceLocation collected(
            UUID deviceId, UUID eventId, TelemetryLocationRequest request, OffsetDateTime receivedAt
    ) {
        return new DeviceLocation(
                eventId,
                deviceId,
                GEOMETRY_FACTORY.createPoint(new Coordinate(request.longitude().doubleValue(), request.latitude().doubleValue())),
                request.accuracyMeters(),
                request.altitudeMeters(),
                request.speedMetersPerSecond(),
                request.provider(),
                request.collectedAt(),
                receivedAt
        );
    }
}
