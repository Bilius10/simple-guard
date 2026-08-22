package simple.guard.api.devices.device.controller.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UnpairDeviceResponse(
    UUID deviceId, String pairingStatus, int revokedKeyCount, OffsetDateTime unpairedAt) {}
