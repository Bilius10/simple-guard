package simple.guard.api.devices;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import simple.guard.api.devices.pairingsession.domain.PairingSession;
import simple.guard.api.devices.pairingsession.domain.PairingSessionExpirationReason;
import simple.guard.api.devices.pairingsession.domain.PairingSessionStatus;

class PairingSessionTests {

  private static final OffsetDateTime CREATED_AT =
      OffsetDateTime.of(2026, 8, 9, 12, 0, 0, 0, ZoneOffset.UTC);

  @Test
  void usesWaitingSessionTests() {
    PairingSession session = waitingSessionTests();
    OffsetDateTime usedAt = CREATED_AT.plusMinutes(4);

    session.use(usedAt);

    assertThat(session.getStatus()).isEqualTo(PairingSessionStatus.USED);
    assertThat(session.getUsedAt()).isEqualTo(usedAt);
  }

  @Test
  void expiresWaitingSessionTests() {
    PairingSession session = waitingSessionTests();
    OffsetDateTime expiredAt = CREATED_AT.plusMinutes(5);

    session.expire(expiredAt, PairingSessionExpirationReason.TIMEOUT);

    assertThat(session.getStatus()).isEqualTo(PairingSessionStatus.EXPIRED);
    assertThat(session.getExpirationReason()).isEqualTo(PairingSessionExpirationReason.TIMEOUT);
    assertThat(session.getExpiredAt()).isEqualTo(expiredAt);
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
        0L);
  }
}
