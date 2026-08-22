package simple.guard.api.devices.devicetelemetry.controller.request;

public record SignedDeviceTelemetryRequest(
        String signature,
        CreateDeviceTelemetryRequest telemetry
) {
}
