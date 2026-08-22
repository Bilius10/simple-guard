package simple.guard.api.devices.pairingsession.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

import static simple.guard.api.shared.i18n.SimpleGuardTranslation.ERROR_SHA_256_NOT_AVAILABLE;

@Component
public class PairingCodeHasher {

    public String hash(String pairingCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(normalize(pairingCode).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(ERROR_SHA_256_NOT_AVAILABLE.name(), exception);
        }
    }

    private String normalize(String pairingCode) {
        return pairingCode.trim().replace("-", "").toUpperCase(Locale.ROOT);
    }
}
