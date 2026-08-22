package simple.guard.api.devices.pairingsession.controller.response;

import java.util.UUID;

public record AgentPairingStatusResponse(
    UUID deviceId, String pairingStatus, String unpairingStatus) {}
