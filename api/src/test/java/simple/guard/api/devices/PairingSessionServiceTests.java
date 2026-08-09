package simple.guard.api.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simple.guard.api.config.SimpleGuardPairingProperties;
import simple.guard.api.devices.management.domain.Device;
import simple.guard.api.devices.management.domain.DevicePairingStatus;
import simple.guard.api.devices.management.domain.DevicePlatform;
import simple.guard.api.devices.management.domain.DeviceType;
import simple.guard.api.devices.management.service.DeviceService;
import simple.guard.api.devices.pairing.domain.PairingSession;
import simple.guard.api.devices.pairing.domain.PairingSessionExpirationReason;
import simple.guard.api.devices.pairing.domain.PairingSessionRepository;
import simple.guard.api.devices.pairing.domain.PairingSessionStatus;
import simple.guard.api.devices.pairing.service.PairingCodeGenerator;
import simple.guard.api.devices.pairing.service.PairingCodeHasher;
import simple.guard.api.devices.pairing.service.PairingSessionService;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.identity.domain.Account;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PairingSessionServiceTests {

    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final OffsetDateTime NOW = OffsetDateTime.of(
            2026, 8, 9, 12, 0, 0, 0, ZoneOffset.UTC
    );

    private DeviceService deviceService;
    private PairingSessionRepository pairingSessions;
    private PairingCodeGenerator codeGenerator;
    private PairingCodeHasher codeHasher;
    private PairingSessionService service;

    @BeforeEach
    void setUpTests() {
        deviceService = mock(DeviceService.class);
        pairingSessions = mock(PairingSessionRepository.class);
        codeGenerator = mock(PairingCodeGenerator.class);
        codeHasher = mock(PairingCodeHasher.class);
        service = new PairingSessionService(
                deviceService,
                pairingSessions,
                codeGenerator,
                codeHasher,
                new SimpleGuardPairingProperties(Duration.ofMinutes(5)),
                Clock.fixed(Instant.parse("2026-08-09T12:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void generatesSessionAndExpiresElapsedWaitingSessionsTests() {
        Account account = accountTests();
        Device device = deviceTests(DevicePairingStatus.UNPAIRED);
        PairingSession elapsedPrevious = waitingSessionTests(NOW.minusSeconds(1));

        when(deviceService.findByIdAndAccountId(DEVICE_ID, ACCOUNT_ID)).thenReturn(device);
        when(pairingSessions.existsByDeviceIdAndStatusAndExpiresAtAfter(DEVICE_ID, PairingSessionStatus.WAITING, NOW))
                .thenReturn(false);
        when(pairingSessions.findAllByDeviceIdAndStatusAndExpiresAtLessThanEqual(
                DEVICE_ID,
                PairingSessionStatus.WAITING,
                NOW
        )).thenReturn(List.of(elapsedPrevious));
        when(codeGenerator.generate()).thenReturn("ABCD-2345");
        when(codeHasher.hash("ABCD-2345")).thenReturn("hashed-code");
        when(pairingSessions.save(any(PairingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.generate(DEVICE_ID, account);

        assertThat(response.deviceId()).isEqualTo(DEVICE_ID);
        assertThat(response.pairingCode()).isEqualTo("ABCD-2345");
        assertThat(response.status()).isEqualTo("waiting");
        assertThat(response.expiresAt()).isEqualTo(NOW.plusMinutes(5));
        assertThat(response.createdAt()).isEqualTo(NOW);
        assertThat(elapsedPrevious.getStatus()).isEqualTo(PairingSessionStatus.EXPIRED);
        assertThat(elapsedPrevious.getExpirationReason()).isEqualTo(PairingSessionExpirationReason.TIMEOUT);
        verify(pairingSessions).saveAllAndFlush(List.of(elapsedPrevious));
    }

    @Test
    void rejectsGenerationForPairedDeviceTests() {
        Account account = accountTests();
        when(deviceService.findByIdAndAccountId(DEVICE_ID, ACCOUNT_ID))
                .thenReturn(deviceTests(DevicePairingStatus.PAIRED));

        assertThatThrownBy(() -> service.generate(DEVICE_ID, account))
                .isInstanceOfSatisfying(SimpleGuardException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(SimpleGuardErrorCode.DEVICE_ALREADY_PAIRED)
                );
    }

    @Test
    void rejectsGenerationForDeviceWithOpenPairingSessionTests() {
        Account account = accountTests();
        when(deviceService.findByIdAndAccountId(DEVICE_ID, ACCOUNT_ID))
                .thenReturn(deviceTests(DevicePairingStatus.UNPAIRED));
        when(pairingSessions.existsByDeviceIdAndStatusAndExpiresAtAfter(DEVICE_ID, PairingSessionStatus.WAITING, NOW))
                .thenReturn(true);

        assertThatThrownBy(() -> service.generate(DEVICE_ID, account))
                .isInstanceOfSatisfying(SimpleGuardException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(SimpleGuardErrorCode.MAX_OPEN_PAIRING_SESSIONS_REACHED)
                );
    }

    @Test
    void expiresElapsedSessionsWithSystemActorTests() {
        PairingSession elapsed = waitingSessionTests(NOW.minusSeconds(1));
        when(pairingSessions.findAllByStatusAndExpiresAtLessThanEqual(PairingSessionStatus.WAITING, NOW))
                .thenReturn(List.of(elapsed));

        service.expireElapsedSessions();

        assertThat(elapsed.getStatus()).isEqualTo(PairingSessionStatus.EXPIRED);
        assertThat(elapsed.getExpirationReason()).isEqualTo(PairingSessionExpirationReason.TIMEOUT);
        assertThat(elapsed.getExpiredAt()).isEqualTo(NOW);
        assertThat(elapsed.getUpdatedBy()).isEqualTo("simpleguard-system");
        assertThat(elapsed.getUpdatedAt()).isEqualTo(NOW);
        verify(pairingSessions).saveAll(List.of(elapsed));
    }

    private static Account accountTests() {
        return new Account(
                ACCOUNT_ID,
                "administrator-subject",
                "admin@simpleguard.local",
                "SimpleGuard Admin",
                "ADMIN",
                true,
                "test",
                NOW,
                "test",
                NOW
        );
    }

    private static Device deviceTests(DevicePairingStatus status) {
        return new Device(
                DEVICE_ID,
                ACCOUNT_ID,
                "Celular operacional",
                DeviceType.MOBILE,
                DevicePlatform.ANDROID,
                status,
                "administrator-subject",
                NOW,
                "administrator-subject",
                NOW
        );
    }

    private static PairingSession waitingSessionTests(OffsetDateTime expiresAt) {
        return new PairingSession(
                UUID.randomUUID(),
                DEVICE_ID,
                ACCOUNT_ID,
                "a".repeat(64),
                PairingSessionStatus.WAITING,
                null,
                expiresAt,
                null,
                null,
                "administrator-subject",
                NOW.minusMinutes(1),
                "administrator-subject",
                NOW.minusMinutes(1),
                0L
        );
    }
}
