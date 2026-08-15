package simple.guard.api.devices;

import org.junit.jupiter.api.Test;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;

import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
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

    private static java.security.KeyPair keyPairTests() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        return generator.generateKeyPair();
    }

    private static String signatureTests(java.security.PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(AgentSignatureVerifier.unpairingPayload(DEVICE_ID, AGENT_INSTANCE_ID));
        return Base64.getEncoder().encodeToString(signature.sign());
    }
}


