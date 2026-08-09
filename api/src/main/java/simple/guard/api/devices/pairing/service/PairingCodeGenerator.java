package simple.guard.api.devices.pairing.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PairingCodeGenerator {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH + 1);
        for (int index = 0; index < CODE_LENGTH; index++) {
            if (index == CODE_LENGTH / 2) {
                code.append('-');
            }
            code.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return code.toString();
    }
}
