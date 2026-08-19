package simple.guard.api.devices.devicelocation.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.devicelocation.controller.request.CreateDeviceLocationRequest;
import simple.guard.api.devices.devicelocation.controller.response.DeviceLocationResponse;
import simple.guard.api.devices.devicelocation.domain.DeviceLocation;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationRepository;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.service.DeviceKeyService;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DeviceLocationService {

    private final DeviceLocationRepository deviceLocations;
    private final DeviceKeyService deviceKeys;
    private final AgentSignatureVerifier signatureVerifier;
    private final Clock clock;

    public DeviceLocationService(
            DeviceLocationRepository deviceLocations,
            DeviceKeyService deviceKeys,
            AgentSignatureVerifier signatureVerifier,
            Clock clock
    ) {
        this.deviceLocations = deviceLocations;
        this.deviceKeys = deviceKeys;
        this.signatureVerifier = signatureVerifier;
        this.clock = clock;
    }

    @Transactional
    public DeviceLocationResponse ingest(UUID deviceId, String agentInstanceId, String signature, CreateDeviceLocationRequest request
    ) {
        DeviceKey deviceKey = deviceKeys.requireByDeviceIdAndAgentInstanceIdAndStatus(
                deviceId, agentInstanceId, DeviceKeyStatus.ACTIVE,
                SimpleGuardErrorCode.DEVICE_CREDENTIAL_REVOKED, SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_REVOKED
        );
        validateSignature(deviceKey, deviceId, agentInstanceId, signature, request);
        DeviceLocation location = createLocation(deviceId, request);
        return DeviceLocationResponse.from(deviceLocations.saveAndFlush(location));
    }

    private void validateSignature(
            DeviceKey deviceKey,
            UUID deviceId,
            String agentInstanceId,
            String signature,
            CreateDeviceLocationRequest request
    ) {
        boolean valid = signatureVerifier.verifyLocation(
                deviceKey.getPublicKey(),
                deviceId,
                agentInstanceId,
                request.collectedAt(),
                request.latitude(),
                request.longitude(),
                request.accuracyMeters(),
                request.altitudeMeters(),
                request.speedMetersPerSecond(),
                request.provider(),
                signature
        );
        if (!valid) {
            throw new SimpleGuardException(
                    HttpStatus.UNAUTHORIZED,
                    SimpleGuardErrorCode.DEVICE_CREDENTIAL_INVALID,
                    SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_INVALID
            );
        }
    }

    private DeviceLocation createLocation(UUID deviceId, CreateDeviceLocationRequest request) {
        return DeviceLocation.collected(
                UUID.randomUUID(),
                deviceId,
                request.latitude(),
                request.longitude(),
                request.accuracyMeters(),
                request.altitudeMeters(),
                request.speedMetersPerSecond(),
                request.provider(),
                request.collectedAt(),
                OffsetDateTime.now(clock)
        );
    }
}
