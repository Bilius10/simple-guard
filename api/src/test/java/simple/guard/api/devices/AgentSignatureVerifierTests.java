package simple.guard.api.devices;

import org.junit.jupiter.api.Test;
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
    void validatesCanonicalLocationSignatureTests() throws Exception {
        var keyPair = keyPairTests();
        OffsetDateTime collectedAt = OffsetDateTime.parse("2026-08-17T09:00:00-03:00");
        byte[] payload = AgentSignatureVerifier.locationPayload(
                DEVICE_ID,
                AGENT_INSTANCE_ID,
                collectedAt,
                new BigDecimal("-23.55052000"),
                new BigDecimal("-46.63330800"),
                new BigDecimal("4.500"),
                null,
                BigDecimal.ZERO,
                "GPS"
        );

        assertThat(new String(payload, StandardCharsets.UTF_8)).isEqualTo(
                "INGEST_LOCATION\n" + DEVICE_ID + "\n" + AGENT_INSTANCE_ID
                        + "\n2026-08-17T12:00:00Z\n-23.55052\n-46.633308\n4.5\n\n0\nGPS"
        );
        assertThat(verifier.verifyLocation(
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                DEVICE_ID,
                AGENT_INSTANCE_ID,
                collectedAt,
                new BigDecimal("-23.55052000"),
                new BigDecimal("-46.63330800"),
                new BigDecimal("4.500"),
                null,
                BigDecimal.ZERO,
                "GPS",
                signatureTests(keyPair.getPrivate(), payload)
        )).isTrue();
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


