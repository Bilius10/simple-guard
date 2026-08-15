package simple.guard.api.devices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyRepository;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.devicekey.service.DeviceKeyService;
import simple.guard.api.devices.device.domain.DevicePlatform;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceKeyRevocationTests {

    private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final String AGENT_INSTANCE_ID = "android-agent-001";

    @Mock
    private DeviceKeyRepository deviceKeys;

    @Test
    void revokesActiveKeyOnlyOnceTests() {
        DeviceKey key = activeKeyTests();
        OffsetDateTime revokedAt = OffsetDateTime.parse("2026-08-11T12:00:00Z");

        assertThat(key.revoke("administrator-subject", revokedAt)).isTrue();
        assertThat(key.getStatus()).isEqualTo(DeviceKeyStatus.REVOKED);
        assertThat(key.getRevokedBy()).isEqualTo("administrator-subject");
        assertThat(key.getRevokedAt()).isEqualTo(revokedAt);
        assertThat(key.revoke("another-actor", revokedAt.plusMinutes(1))).isFalse();
        assertThat(key.getRevokedBy()).isEqualTo("administrator-subject");
        assertThat(key.getRevokedAt()).isEqualTo(revokedAt);
    }

    @Test
    void acceptsTelemetryOnlyForActiveCredentialTests() {
        DeviceKey key = activeKeyTests();
        when(deviceKeys.findByDeviceIdAndAgentInstanceIdAndStatus(
                DEVICE_ID, AGENT_INSTANCE_ID, DeviceKeyStatus.ACTIVE
        )).thenReturn(Optional.of(key));

        DeviceKeyService service = new DeviceKeyService(deviceKeys);

        assertThat(service.requireActiveForTelemetry(DEVICE_ID, AGENT_INSTANCE_ID)).isSameAs(key);
    }

    @Test
    void rejectsTelemetryAfterCredentialRevocationTests() {
        when(deviceKeys.findByDeviceIdAndAgentInstanceIdAndStatus(
                DEVICE_ID, AGENT_INSTANCE_ID, DeviceKeyStatus.ACTIVE
        )).thenReturn(Optional.empty());

        DeviceKeyService service = new DeviceKeyService(deviceKeys);

        assertThatThrownBy(() -> service.requireActiveForTelemetry(DEVICE_ID, AGENT_INSTANCE_ID))
                .isInstanceOf(SimpleGuardException.class)
                .satisfies(exception -> assertThat(((SimpleGuardException) exception).errorCode())
                        .isEqualTo(SimpleGuardErrorCode.DEVICE_CREDENTIAL_REVOKED));
    }

    private static DeviceKey activeKeyTests() {
        return DeviceKey.active(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                AGENT_INSTANCE_ID,
                DevicePlatform.ANDROID,
                "public-key-" + "A".repeat(80)
        );
    }
}


