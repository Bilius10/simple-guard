package simple.guard.api.devices.devicetelemetry.controller.response;

import java.util.UUID;

public record DeviceTelemetryBatchItemResponse(
    int index, UUID eventId, DeviceTelemetryBatchItemStatus status, String error) {}
