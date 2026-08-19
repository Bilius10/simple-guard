package simple.guard.api.devices.pairingsession.service;

import org.springframework.stereotype.Component;

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

    public boolean verifyLocation(
            String encodedPublicKey,
            UUID deviceId,
            String agentInstanceId,
            OffsetDateTime collectedAt,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracyMeters,
            BigDecimal altitudeMeters,
            BigDecimal speedMetersPerSecond,
            String provider,
            String encodedSignature
    ) {
        return verify(encodedPublicKey, locationPayload(
                deviceId,
                agentInstanceId,
                collectedAt,
                latitude,
                longitude,
                accuracyMeters,
                altitudeMeters,
                speedMetersPerSecond,
                provider
        ), encodedSignature);
    }

    public static byte[] locationPayload(
            UUID deviceId,
            String agentInstanceId,
            OffsetDateTime collectedAt,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracyMeters,
            BigDecimal altitudeMeters,
            BigDecimal speedMetersPerSecond,
            String provider
    ) {
        return String.join(
                "\n",
                "INGEST_LOCATION",
                deviceId.toString(),
                agentInstanceId,
                collectedAt.toInstant().toString(),
                canonicalNumber(latitude),
                canonicalNumber(longitude),
                canonicalNumber(accuracyMeters),
                canonicalNumber(altitudeMeters),
                canonicalNumber(speedMetersPerSecond),
                provider
        ).getBytes(StandardCharsets.UTF_8);
    }

    private static String canonicalNumber(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
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


