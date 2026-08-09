package simple.guard.api.devices.pairing.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PairingSessionRepository extends JpaRepository<PairingSession, UUID> {

    List<PairingSession> findAllByDeviceIdAndStatusAndExpiresAtLessThanEqual(
            UUID deviceId,
            PairingSessionStatus status,
            OffsetDateTime expiresAt
    );

    List<PairingSession> findAllByStatusAndExpiresAtLessThanEqual(
            PairingSessionStatus status,
            OffsetDateTime expiresAt
    );

    boolean existsByDeviceIdAndStatusAndExpiresAtAfter(
            UUID deviceId,
            PairingSessionStatus status,
            OffsetDateTime expiresAt
    );
}
