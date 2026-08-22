package simple.guard.api.devices.devicetelemetry.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.devicekey.service.DeviceKeyService;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationResolution;
import simple.guard.api.devices.devicelocation.service.DeviceLocationService;
import simple.guard.api.devices.devicetelemetry.controller.request.CreateDeviceTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.response.DeviceTelemetryResponse;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetry;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetryRepository;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetryResolution;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

@Service
public class DeviceTelemetryService {

  private final DeviceLocationService locations;
  private final DeviceTelemetryRepository technicalTelemetry;
  private final DeviceKeyService deviceKeys;
  private final AgentSignatureVerifier signatureVerifier;
  private final Clock clock;

  public DeviceTelemetryService(
      DeviceLocationService locations,
      DeviceTelemetryRepository technicalTelemetry,
      DeviceKeyService deviceKeys,
      AgentSignatureVerifier signatureVerifier,
      Clock clock) {
    this.locations = locations;
    this.technicalTelemetry = technicalTelemetry;
    this.deviceKeys = deviceKeys;
    this.signatureVerifier = signatureVerifier;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public Optional<DeviceTelemetry> findFirstByDeviceIdOrderByCollectedAtDesc(UUID deviceId) {
    return technicalTelemetry.findFirstByDeviceIdOrderByCollectedAtDesc(deviceId);
  }

  @Transactional(readOnly = true)
  public List<DeviceTelemetry> findLatestByDeviceIds(Collection<UUID> deviceIds) {
    return technicalTelemetry.findLatestByDeviceIds(deviceIds);
  }

  @Transactional
  public DeviceTelemetryResponse ingest(
      UUID deviceId,
      String agentInstanceId,
      String signature,
      CreateDeviceTelemetryRequest request) {
    DeviceKey deviceKey =
        deviceKeys.requireByDeviceIdAndAgentInstanceIdAndStatus(
            deviceId,
            agentInstanceId,
            DeviceKeyStatus.ACTIVE,
            SimpleGuardErrorCode.DEVICE_CREDENTIAL_REVOKED,
            SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_REVOKED);

    validateSignature(deviceKey, deviceId, agentInstanceId, signature, request);

    OffsetDateTime receivedAt = OffsetDateTime.now(clock);

    DeviceLocationResolution location =
        locations.resolveLocation(deviceId, request.eventId(), request.location(), receivedAt);
    DeviceTelemetryResolution technical = resolveTechnical(deviceId, request, receivedAt);

    return new DeviceTelemetryResponse(
        request.eventId(),
        deviceId,
        location.location() == null ? null : location.location().getId(),
        technical.technical() == null ? null : technical.technical().getId(),
        !location.created() && !technical.created());
  }

  private DeviceTelemetryResolution resolveTechnical(
      UUID deviceId, CreateDeviceTelemetryRequest request, OffsetDateTime receivedAt) {
    if (request.technical() == null) {
      return new DeviceTelemetryResolution(null, false);
    }

    DeviceTelemetry existing = technicalTelemetry.findById(request.eventId()).orElse(null);
    if (existing != null) {
      return new DeviceTelemetryResolution(existing, false);
    }

    return new DeviceTelemetryResolution(
        technicalTelemetry.saveAndFlush(
            new DeviceTelemetry(deviceId, request.eventId(), request.technical(), receivedAt)),
        true);
  }

  private void validateSignature(
      DeviceKey deviceKey,
      UUID deviceId,
      String agentInstanceId,
      String signature,
      CreateDeviceTelemetryRequest request) {
    if (!signatureVerifier.verifyTelemetry(
        deviceKey.getPublicKey(), deviceId, agentInstanceId, request, signature)) {
      throw new SimpleGuardException(
          HttpStatus.UNAUTHORIZED,
          SimpleGuardErrorCode.DEVICE_CREDENTIAL_INVALID,
          SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_INVALID);
    }
  }
}
