package simple.guard.api.devices.management.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "devices")
public class Device {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeviceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DevicePlatform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DevicePairingStatus pairingStatus;

    @Column(nullable = false, length = 128)
    @CreatedBy
    private String createdBy;

    @Column(nullable = false)
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column(nullable = false, length = 128)
    @LastModifiedBy
    private String updatedBy;

    @Column(nullable = false)
    @LastModifiedDate
    private OffsetDateTime updatedAt;

    public Device(
            UUID id,
            UUID accountId,
            String name,
            DeviceType type,
            DevicePlatform platform,
            DevicePairingStatus pairingStatus
    ) {
        this.id = id;
        this.accountId = accountId;
        this.name = name;
        this.type = type;
        this.platform = platform;
        this.pairingStatus = pairingStatus;
    }
}
