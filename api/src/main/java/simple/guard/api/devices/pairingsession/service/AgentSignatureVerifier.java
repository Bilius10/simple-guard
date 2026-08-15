package simple.guard.api.devices.pairingsession.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Component
public class AgentSignatureVerifier {

    public boolean verifyUnpairing(String encodedPublicKey, UUID deviceId, String agentInstanceId, String encodedSignature) {
        try {
            var publicKey = KeyFactory.getInstance("EC").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(encodedPublicKey))
            );
            Signature verifier = Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(publicKey);
            verifier.update(unpairingPayload(deviceId, agentInstanceId));
            return verifier.verify(Base64.getDecoder().decode(encodedSignature));
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            return false;
        }
    }

    public static byte[] unpairingPayload(UUID deviceId, String agentInstanceId) {
        return ("UNPAIR_DEVICE\n" + deviceId + "\n" + agentInstanceId)
                .getBytes(StandardCharsets.UTF_8);
    }
}


