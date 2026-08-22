package simple.guard.api.devices.pairingsession.domain;

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
@Table(name = "pairing_sessions")
public class PairingSession {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false, unique = true, length = 64)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PairingSessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private PairingSessionExpirationReason expirationReason;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    private OffsetDateTime usedAt;

    private OffsetDateTime expiredAt;

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

    @Version
    @Column(nullable = false)
    private long version;

    public void expire(OffsetDateTime now, PairingSessionExpirationReason reason) {
        status = PairingSessionStatus.EXPIRED;
        expirationReason = reason;
        expiredAt = now;
    }

    public void use(OffsetDateTime now) {
        status = PairingSessionStatus.USED;
        usedAt = now;
    }

    public PairingSession(
            UUID id,
            UUID deviceId,
            UUID accountId,
            String codeHash,
            PairingSessionStatus status,
            PairingSessionExpirationReason expirationReason,
            OffsetDateTime expiresAt,
            OffsetDateTime usedAt,
            OffsetDateTime expiredAt
    ) {
        this.id = id;
        this.deviceId = deviceId;
        this.accountId = accountId;
        this.codeHash = codeHash;
        this.status = status;
        this.expirationReason = expirationReason;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }
}
