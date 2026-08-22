package simple.guard.api.devices;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import simple.guard.api.devices.pairingsession.service.PairingCodeHasher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

class PairingCodeHasherTests {

    private final PairingCodeHasher hasher = new PairingCodeHasher();

    @Test
    void normalizesPairingCodeBeforeHashingTests() throws Exception {
        String expected = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest("ABCD2345".getBytes(StandardCharsets.UTF_8))
        );

        assertThat(hasher.hash(" abcd-2345 ")).isEqualTo(expected);
    }

    @Test
    void failsFastWhenSha256IsUnavailableTests() {
        try (MockedStatic<MessageDigest> digests = mockStatic(MessageDigest.class)) {
            digests.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("missing"));

            assertThatThrownBy(() -> hasher.hash("ABCD-2345"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("ERROR_SHA_256_NOT_AVAILABLE")
                    .hasCauseInstanceOf(NoSuchAlgorithmException.class);
        }
    }
}
