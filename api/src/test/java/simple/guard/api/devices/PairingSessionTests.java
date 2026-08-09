package simple.guard.api.devices;

import org.junit.jupiter.api.Test;
import simple.guard.api.devices.pairing.domain.PairingSession;
import simple.guard.api.devices.pairing.domain.PairingSessionExpirationReason;
import simple.guard.api.devices.pairing.domain.PairingSessionStatus;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PairingSessionTests {

    private static final OffsetDateTime CREATED_AT = OffsetDateTime.of(
            2026, 8, 9, 12, 0, 0, 0, ZoneOffset.UTC
    );

    @Test
    void acceptsWaitingSessionBeforeExpirationTests() {
        PairingSession session = waitingSessionTests();

        assertThat(session.isValidAt(CREATED_AT.plusMinutes(4))).isTrue();
    }

    @Test
    void rejectsSessionWhenStatusIsNotWaitingTests() {
        PairingSession session = waitingSessionTests();
        session.use(CREATED_AT.plusMinutes(1), "agent");

        assertThat(session.isValidAt(CREATED_AT.plusMinutes(2))).isFalse();
    }

    @Test
    void rejectsAndAuditsExpiredSessionTests() {
        PairingSession session = waitingSessionTests();
        OffsetDateTime attemptedAt = CREATED_AT.plusMinutes(5);

        assertThatThrownBy(() -> session.use(attemptedAt, "agent"))
                .isInstanceOfSatisfying(SimpleGuardException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(SimpleGuardErrorCode.PAIRING_SESSION_EXPIRED)
                );
        assertThat(session.getStatus()).isEqualTo(PairingSessionStatus.EXPIRED);
        assertThat(session.getExpirationReason()).isEqualTo(PairingSessionExpirationReason.TIMEOUT);
        assertThat(session.getExpiredAt()).isEqualTo(attemptedAt);
    }

    @Test
    void blocksPairingSessionReuseTests() {
        PairingSession session = waitingSessionTests();
        session.use(CREATED_AT.plusMinutes(1), "agent");

        assertThatThrownBy(() -> session.use(CREATED_AT.plusMinutes(2), "agent"))
                .isInstanceOfSatisfying(SimpleGuardException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(
                                SimpleGuardErrorCode.PAIRING_SESSION_ALREADY_USED
                        )
                );
        assertThat(session.getStatus()).isEqualTo(PairingSessionStatus.USED);
        assertThat(session.getUsedAt()).isEqualTo(CREATED_AT.plusMinutes(1));
    }

    @Test
    void ignoresExpirationWhenSessionIsNotWaitingTests() {
        PairingSession session = waitingSessionTests();
        session.use(CREATED_AT.plusMinutes(1), "agent");

        session.expire(
                CREATED_AT.plusMinutes(2),
                "simpleguard-system",
                PairingSessionExpirationReason.TIMEOUT
        );

        assertThat(session.getStatus()).isEqualTo(PairingSessionStatus.USED);
        assertThat(session.getExpirationReason()).isNull();
        assertThat(session.getExpiredAt()).isNull();
        assertThat(session.getUpdatedBy()).isEqualTo("agent");
        assertThat(session.getUpdatedAt()).isEqualTo(CREATED_AT.plusMinutes(1));
    }

    private PairingSession waitingSessionTests() {
        return new PairingSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "a".repeat(64),
                PairingSessionStatus.WAITING,
                null,
                CREATED_AT.plusMinutes(5),
                null,
                null,
                "admin",
                CREATED_AT,
                "admin",
                CREATED_AT,
                0L
        );
    }
}
