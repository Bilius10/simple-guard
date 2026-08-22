package simple.guard.api.devices.devicetelemetry.controller.response;

import java.util.UUID;

public record DeviceTelemetryResponse(
    UUID eventId, UUID deviceId, UUID locationId, UUID technicalTelemetryId, boolean duplicate) {}
