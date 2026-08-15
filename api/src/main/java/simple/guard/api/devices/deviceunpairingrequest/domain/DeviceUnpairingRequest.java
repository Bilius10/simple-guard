package simple.guard.api.devices.deviceunpairingrequest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import simple.guard.api.devices.device.domain.Device;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "device_unpairing_requests")
public class DeviceUnpairingRequest {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false, length = 128)
    private String agentInstanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeviceUnpairingRequestStatus status;

    @Column(nullable = false, length = 128)
    private String requestedBy;

    @Column(nullable = false)
    private OffsetDateTime requestedAt;

    @Column(length = 128)
    private String decidedBy;

    private OffsetDateTime decidedAt;

    @Version
    @Column(nullable = false)
    private long version;

    public static DeviceUnpairingRequest pending(
            UUID id,
            Device device,
            String agentInstanceId,
            OffsetDateTime requestedAt
    ) {
        String actor = "agent:" + agentInstanceId;
        return new DeviceUnpairingRequest(
                id,
                device.getId(),
                device.getAccountId(),
                agentInstanceId,
                DeviceUnpairingRequestStatus.PENDING,
                actor,
                requestedAt,
                null,
                null,
                0L
        );
    }

    public void respondeUnpairingRequest(String actor, OffsetDateTime decidedAt, DeviceUnpairingRequestStatus status) {
        this.status = status;
        decidedBy = actor;
        this.decidedAt = decidedAt;
    }
}


