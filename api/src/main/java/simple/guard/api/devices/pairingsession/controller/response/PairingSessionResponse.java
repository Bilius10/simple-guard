package simple.guard.api.devices.pairingsession.controller.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import simple.guard.api.devices.pairingsession.domain.PairingSession;

public record PairingSessionResponse(
    UUID pairingSessionId,
    UUID deviceId,
    String pairingCode,
    String status,
    OffsetDateTime expiresAt,
    OffsetDateTime createdAt) {

  public static PairingSessionResponse from(PairingSession session, String pairingCode) {
    return new PairingSessionResponse(
        session.getId(),
        session.getDeviceId(),
        pairingCode,
        session.getStatus().apiValue(),
        session.getExpiresAt(),
        session.getCreatedAt());
  }
}
