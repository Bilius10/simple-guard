package simple.guard.api.devices;

import org.junit.jupiter.api.Test;
import simple.guard.api.devices.devicetelemetry.controller.request.CreateDeviceTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TechnicalTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryLocationRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryPermissionsRequest;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;

import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AgentSignatureVerifierTests {

    private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000601");
    private static final String AGENT_INSTANCE_ID = "android-agent-signature";

    private final AgentSignatureVerifier verifier = new AgentSignatureVerifier();

    @Test
    void validatesEcdsaProofOfPossessionTests() throws Exception {
        var keyPair = keyPairTests();
        String signature = signatureTests(keyPair.getPrivate());

        assertThat(verifier.verifyUnpairing(
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                DEVICE_ID,
                AGENT_INSTANCE_ID,
                signature
        )).isTrue();
    }

    @Test
    void rejectsModifiedOrMalformedSignatureTests() throws Exception {
        var keyPair = keyPairTests();

        assertThat(verifier.verifyUnpairing(
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                DEVICE_ID,
                AGENT_INSTANCE_ID + "-modified",
                signatureTests(keyPair.getPrivate())
        )).isFalse();
        assertThat(verifier.verifyUnpairing("not-base64", DEVICE_ID, AGENT_INSTANCE_ID, "not-base64"))
                .isFalse();
        assertThat(verifier.verifyUnpairing(
                Base64.getEncoder().encodeToString("not-a-public-key".getBytes()),
                DEVICE_ID,
                AGENT_INSTANCE_ID,
                Base64.getEncoder().encodeToString("invalid-signature".getBytes())
        )).isFalse();
    }

    @Test
    void validatesCanonicalTelemetrySignatureTests() throws Exception {
        var keyPair = keyPairTests();
        OffsetDateTime collectedAt = OffsetDateTime.parse("2026-08-17T09:00:00-03:00");
        UUID eventId = UUID.fromString("00000000-0000-0000-0000-000000000602");
        CreateDeviceTelemetryRequest request = new CreateDeviceTelemetryRequest(
                eventId,
                new TelemetryLocationRequest(
                        new BigDecimal("-23.55052000"),
                        new BigDecimal("-46.63330800"),
                        new BigDecimal("4.500"),
                        null,
                        BigDecimal.ZERO,
                        "GPS",
                        collectedAt
                ),
                new TechnicalTelemetryRequest(
                        0,
                        false,
                        "CELLULAR",
                        -95,
                        new TelemetryPermissionsRequest("GRANTED", null),
                        collectedAt.plusSeconds(1)
                )
        );
        byte[] payload = AgentSignatureVerifier.telemetryPayload(DEVICE_ID, AGENT_INSTANCE_ID, request);

        assertThat(new String(payload, StandardCharsets.UTF_8)).isEqualTo(
                "INGEST_TELEMETRY\n" + DEVICE_ID + "\n" + AGENT_INSTANCE_ID + "\n" + eventId
                        + "\n1\n2026-08-17T12:00:00Z\n-23.55052\n-46.633308\n4.5\n\n0\nGPS"
                        + "\n1\n2026-08-17T12:00:01Z\n0\nfalse\nCELLULAR\n-95\nGRANTED\n"
        );
        assertThat(verifier.verifyTelemetry(
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                DEVICE_ID,
                AGENT_INSTANCE_ID,
                request,
                signatureTests(keyPair.getPrivate(), payload)
        )).isTrue();
    }

    @Test
    void canonicalizesMissingTelemetryBlocksAndValuesTests() {
        UUID eventId = UUID.fromString("00000000-0000-0000-0000-000000000603");
        CreateDeviceTelemetryRequest request = new CreateDeviceTelemetryRequest(
                eventId,
                null,
                new TechnicalTelemetryRequest(
                        null,
                        null,
                        null,
                        null,
                        new TelemetryPermissionsRequest(null, "DENIED"),
                        OffsetDateTime.parse("2026-08-17T12:00:00Z")
                )
        );

        assertThat(new String(
                AgentSignatureVerifier.telemetryPayload(DEVICE_ID, AGENT_INSTANCE_ID, request),
                StandardCharsets.UTF_8
        )).isEqualTo(
                "INGEST_TELEMETRY\n" + DEVICE_ID + "\n" + AGENT_INSTANCE_ID + "\n" + eventId
                        + "\n0\n\n\n\n\n\n\n\n1\n2026-08-17T12:00:00Z\n\n\n\n\n\nDENIED"
        );
    }

    private static java.security.KeyPair keyPairTests() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        return generator.generateKeyPair();
    }

    private static String signatureTests(java.security.PrivateKey privateKey) throws Exception {
        return signatureTests(
                privateKey,
                AgentSignatureVerifier.unpairingPayload(DEVICE_ID, AGENT_INSTANCE_ID)
        );
    }

    private static String signatureTests(java.security.PrivateKey privateKey, byte[] payload) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(payload);
        return Base64.getEncoder().encodeToString(signature.sign());
    }
}
