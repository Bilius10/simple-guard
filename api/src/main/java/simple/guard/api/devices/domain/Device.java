package simple.guard.api.devices.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private String createdBy;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false, length = 128)
    private String updatedBy;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

}
