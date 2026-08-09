package simple.guard.api.devices.pairing.domain;

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
import org.springframework.http.HttpStatus;
import simple.guard.api.devices.pairing.domain.PairingSessionExpirationReason;
import simple.guard.api.devices.pairing.domain.PairingSessionStatus;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private String createdBy;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false, length = 128)
    private String updatedBy;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    public boolean isValidAt(OffsetDateTime now) {
        return status == PairingSessionStatus.WAITING && now.isBefore(expiresAt);
    }

    public void expire(OffsetDateTime now, String actor, PairingSessionExpirationReason reason) {
        if (status != PairingSessionStatus.WAITING) {
            return;
        }

        status = PairingSessionStatus.EXPIRED;
        expirationReason = reason;
        expiredAt = now;
        updatedBy = actor;
        updatedAt = now;
    }

    public void use(OffsetDateTime now, String actor) {
        if (status == PairingSessionStatus.USED) {
            throw new SimpleGuardException(
                    HttpStatus.CONFLICT,
                    SimpleGuardErrorCode.PAIRING_SESSION_ALREADY_USED,
                    SimpleGuardTranslation.ERROR_PAIRING_SESSION_ALREADY_USED
            );
        }

        if (!isValidAt(now)) {
            expire(now, actor, PairingSessionExpirationReason.TIMEOUT);
            throw new SimpleGuardException(
                    HttpStatus.GONE,
                    SimpleGuardErrorCode.PAIRING_SESSION_EXPIRED,
                    SimpleGuardTranslation.ERROR_PAIRING_SESSION_EXPIRED
            );
        }

        status = PairingSessionStatus.USED;
        usedAt = now;
        updatedBy = actor;
        updatedAt = now;
    }
}
