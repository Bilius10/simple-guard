package simple.guard.api.devices.devicekey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import simple.guard.api.devices.device.domain.DevicePlatform;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "device_keys")
public class DeviceKey {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    @Column(nullable = false)
    private UUID pairingSessionId;

    @Column(nullable = false, length = 128)
    private String agentInstanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DevicePlatform platform;

    @Column(nullable = false)
    private String publicKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeviceKeyStatus status;

    @Column(nullable = false, length = 128)
    @CreatedBy
    private String createdBy;

    @Column(nullable = false)
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column(length = 128)
    private String revokedBy;

    private OffsetDateTime revokedAt;

    @Version
    @Column(nullable = false)
    private long version;

    public static DeviceKey active(
            UUID id,
            UUID deviceId,
            UUID pairingSessionId,
            String agentInstanceId,
            DevicePlatform platform,
            String publicKey
    ) {
        return new DeviceKey(
                id,
                deviceId,
                pairingSessionId,
                agentInstanceId,
                platform,
                publicKey,
                DeviceKeyStatus.ACTIVE,
                null,
                null,
                null,
                null,
                0L
        );
    }

    public boolean revoke(String actor, OffsetDateTime revokedAt) {
        if (status == DeviceKeyStatus.REVOKED) {
            return false;
        }

        status = DeviceKeyStatus.REVOKED;
        revokedBy = actor;
        this.revokedAt = revokedAt;
        return true;
    }
}


