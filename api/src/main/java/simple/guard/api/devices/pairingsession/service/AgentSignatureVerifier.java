package simple.guard.api.devices.pairingsession.service;

import org.springframework.stereotype.Component;
import simple.guard.api.devices.devicetelemetry.controller.request.CreateDeviceTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TechnicalTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryLocationRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryPermissionsRequest;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

@Component
public class AgentSignatureVerifier {

    public boolean verifyUnpairing(String encodedPublicKey, UUID deviceId, String agentInstanceId, String encodedSignature) {
        return verify(encodedPublicKey, unpairingPayload(deviceId, agentInstanceId), encodedSignature);
    }

    public static byte[] unpairingPayload(UUID deviceId, String agentInstanceId) {
        return ("UNPAIR_DEVICE\n" + deviceId + "\n" + agentInstanceId)
                .getBytes(StandardCharsets.UTF_8);
    }

    public boolean verifyTelemetry(
            String encodedPublicKey,
            UUID deviceId,
            String agentInstanceId,
            CreateDeviceTelemetryRequest request,
            String encodedSignature
    ) {
        return verify(encodedPublicKey, telemetryPayload(deviceId, agentInstanceId, request), encodedSignature);
    }

    public static byte[] telemetryPayload(
            UUID deviceId,
            String agentInstanceId,
            CreateDeviceTelemetryRequest request
    ) {
        TelemetryLocationRequest location = request.location();
        TechnicalTelemetryRequest technical = request.technical();
        TelemetryPermissionsRequest permissions = technical == null ? null : technical.permissions();
        return String.join(
                "\n",
                "INGEST_TELEMETRY",
                deviceId.toString(),
                agentInstanceId,
                request.eventId().toString(),
                location == null ? "0" : "1",
                canonicalTimestamp(location == null ? null : location.collectedAt()),
                canonicalNumber(location == null ? null : location.latitude()),
                canonicalNumber(location == null ? null : location.longitude()),
                canonicalNumber(location == null ? null : location.accuracyMeters()),
                canonicalNumber(location == null ? null : location.altitudeMeters()),
                canonicalNumber(location == null ? null : location.speedMetersPerSecond()),
                location == null ? "" : location.provider(),
                technical == null ? "0" : "1",
                canonicalTimestamp(technical == null ? null : technical.collectedAt()),
                canonicalValue(technical == null ? null : technical.batteryLevelPercentage()),
                canonicalValue(technical == null ? null : technical.batteryCharging()),
                technical == null || technical.networkType() == null ? "" : technical.networkType(),
                canonicalValue(technical == null ? null : technical.signalStrengthDbm()),
                permissions == null || permissions.fineLocation() == null ? "" : permissions.fineLocation(),
                permissions == null || permissions.coarseLocation() == null ? "" : permissions.coarseLocation()
        ).getBytes(StandardCharsets.UTF_8);
    }

    private static String canonicalTimestamp(OffsetDateTime value) {
        return value == null ? "" : value.toInstant().toString();
    }

    private static String canonicalNumber(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private static String canonicalValue(Object value) {
        return value == null ? "" : value.toString().toLowerCase();
    }

    private boolean verify(String encodedPublicKey, byte[] payload, String encodedSignature) {
        try {
            var publicKey = KeyFactory.getInstance("EC").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(encodedPublicKey))
            );
            Signature verifier = Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(publicKey);
            verifier.update(payload);
            return verifier.verify(Base64.getDecoder().decode(encodedSignature));
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            return false;
        }
    }
}
