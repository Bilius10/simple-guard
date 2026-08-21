package simple.guard.api.devices.devicetelemetry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import simple.guard.api.devices.devicetelemetry.controller.request.TechnicalTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryPermissionsRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "device_telemetry")
public class DeviceTelemetry {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    private Integer batteryLevelPercentage;
    private Boolean batteryCharging;

    @Column(length = 16)
    private String networkType;

    private Integer signalStrengthDbm;

    @Column(length = 16)
    private String fineLocationPermission;

    @Column(length = 16)
    private String coarseLocationPermission;

    @Column(nullable = false)
    private OffsetDateTime collectedAt;

    @Column(nullable = false)
    private OffsetDateTime receivedAt;

    public DeviceTelemetry(UUID deviceId, UUID eventId, TechnicalTelemetryRequest request, OffsetDateTime receivedAt) {
        TelemetryPermissionsRequest permissions = request.permissions();

        this.id = eventId;
        this.deviceId = deviceId;
        this.batteryLevelPercentage = request.batteryLevelPercentage();
        this.batteryCharging = request.batteryCharging();
        this.networkType = request.networkType();
        this.signalStrengthDbm = request.signalStrengthDbm();
        this.fineLocationPermission = permissions == null ? null : permissions.fineLocation();
        this.coarseLocationPermission = permissions == null ? null : permissions.coarseLocation();
        this.collectedAt = request.collectedAt();
        this.receivedAt = receivedAt;
    }
}
