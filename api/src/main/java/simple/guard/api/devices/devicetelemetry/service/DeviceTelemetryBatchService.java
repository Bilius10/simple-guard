package simple.guard.api.devices.devicetelemetry.service;

import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.devicekey.service.DeviceKeyService;
import simple.guard.api.devices.devicetelemetry.controller.request.CreateDeviceTelemetryBatchRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.CreateDeviceTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.SignedDeviceTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.response.DeviceTelemetryBatchItemResponse;
import simple.guard.api.devices.devicetelemetry.controller.response.DeviceTelemetryBatchItemStatus;
import simple.guard.api.devices.devicetelemetry.controller.response.DeviceTelemetryBatchResponse;
import simple.guard.api.devices.devicetelemetry.controller.response.DeviceTelemetryResponse;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

@Service
public class DeviceTelemetryBatchService {

  private final DeviceTelemetryService telemetry;
  private final DeviceKeyService deviceKeys;
  private final AgentSignatureVerifier signatureVerifier;
  private final Validator validator;
  private final MessageSource messageSource;

  public DeviceTelemetryBatchService(
      DeviceTelemetryService telemetry,
      DeviceKeyService deviceKeys,
      AgentSignatureVerifier signatureVerifier,
      Validator validator,
      MessageSource messageSource) {
    this.telemetry = telemetry;
    this.deviceKeys = deviceKeys;
    this.signatureVerifier = signatureVerifier;
    this.validator = validator;
    this.messageSource = messageSource;
  }

  public DeviceTelemetryBatchResponse ingest(
      UUID deviceId, String agentInstanceId, CreateDeviceTelemetryBatchRequest request) {
    DeviceKey deviceKey =
        deviceKeys.requireByDeviceIdAndAgentInstanceIdAndStatus(
            deviceId,
            agentInstanceId,
            DeviceKeyStatus.ACTIVE,
            SimpleGuardErrorCode.DEVICE_CREDENTIAL_REVOKED,
            SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_REVOKED);

    List<DeviceTelemetryBatchItemResponse> results = new ArrayList<>();

    for (int index = 0; index < request.events().size(); index++) {
      results.add(
          ingestItem(deviceKey, deviceId, agentInstanceId, index, request.events().get(index)));
    }
    return new DeviceTelemetryBatchResponse(List.copyOf(results));
  }

  private DeviceTelemetryBatchItemResponse ingestItem(
      DeviceKey deviceKey,
      UUID deviceId,
      String agentInstanceId,
      int index,
      SignedDeviceTelemetryRequest item) {
    DeviceTelemetryBatchItemResponse requiredTelemetryError =
        validateRequiredTelemetry(index, item);
    if (requiredTelemetryError != null) {
      return requiredTelemetryError;
    }

    DeviceTelemetryBatchItemResponse telemetryError = validateTelemetry(index, item.telemetry());
    if (telemetryError != null) {
      return telemetryError;
    }

    DeviceTelemetryBatchItemResponse signatureError =
        validateSignature(deviceKey, deviceId, agentInstanceId, index, item);
    if (signatureError != null) {
      return signatureError;
    }

    DeviceTelemetryResponse response =
        telemetry.ingest(deviceId, agentInstanceId, item.signature(), item.telemetry());

    return new DeviceTelemetryBatchItemResponse(
        index,
        item.telemetry().eventId(),
        response.duplicate()
            ? DeviceTelemetryBatchItemStatus.DUPLICATE
            : DeviceTelemetryBatchItemStatus.ACCEPTED,
        null);
  }

  private DeviceTelemetryBatchItemResponse validateRequiredTelemetry(
      int index, SignedDeviceTelemetryRequest item) {
    if (item == null || item.telemetry() == null) {
      return errorResult(
          index,
          null,
          DeviceTelemetryBatchItemStatus.INVALID,
          SimpleGuardTranslation.ERROR_TELEMETRY_BATCH_ITEM_REQUIRED);
    }
    return null;
  }

  private DeviceTelemetryBatchItemResponse validateTelemetry(
      int index, CreateDeviceTelemetryRequest request) {
    if (!validator.validate(request).isEmpty()) {
      return errorResult(
          index,
          request.eventId(),
          DeviceTelemetryBatchItemStatus.INVALID,
          SimpleGuardTranslation.ERROR_TELEMETRY_BATCH_ITEM_INVALID);
    }
    return null;
  }

  private DeviceTelemetryBatchItemResponse validateSignature(
      DeviceKey deviceKey,
      UUID deviceId,
      String agentInstanceId,
      int index,
      SignedDeviceTelemetryRequest item) {
    CreateDeviceTelemetryRequest request = item.telemetry();
    if (item.signature() == null
        || item.signature().isBlank()
        || !signatureVerifier.verifyTelemetry(
            deviceKey.getPublicKey(), deviceId, agentInstanceId, request, item.signature())) {
      return errorResult(
          index,
          request.eventId(),
          DeviceTelemetryBatchItemStatus.UNAUTHORIZED,
          SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_INVALID);
    }
    return null;
  }

  private DeviceTelemetryBatchItemResponse errorResult(
      int index,
      UUID eventId,
      DeviceTelemetryBatchItemStatus status,
      SimpleGuardTranslation translation) {
    String message =
        messageSource.getMessage(
            translation.key(), null, translation.key(), LocaleContextHolder.getLocale());
    return new DeviceTelemetryBatchItemResponse(index, eventId, status, message);
  }
}
