package simple.guard.api.devices.deviceunpairingrequest.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.devices.device.controller.response.UnpairDeviceResponse;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.service.DeviceService;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.devicekey.service.DeviceKeyService;
import simple.guard.api.devices.deviceunpairingrequest.controller.response.DeviceUnpairingDecisionResponse;
import simple.guard.api.devices.deviceunpairingrequest.controller.response.DeviceUnpairingRequestResponse;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequest;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestRepository;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestStatus;
import simple.guard.api.devices.pairingsession.controller.response.AgentPairingStatusResponse;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

@Service
public class DeviceUnpairingRequestService {

  private final DeviceUnpairingRequestRepository requests;
  private final DeviceService deviceService;
  private final DeviceKeyService deviceKeys;
  private final AgentSignatureVerifier signatureVerifier;
  private final Clock clock;

  public DeviceUnpairingRequestService(
      DeviceUnpairingRequestRepository requests,
      DeviceService deviceService,
      DeviceKeyService deviceKeys,
      AgentSignatureVerifier signatureVerifier,
      Clock clock) {
    this.requests = requests;
    this.deviceService = deviceService;
    this.deviceKeys = deviceKeys;
    this.signatureVerifier = signatureVerifier;
    this.clock = clock;
  }

  @Transactional
  public DeviceUnpairingRequestResponse requestByAgent(
      UUID deviceId, String agentInstanceId, String signature) {
    String normalizedAgentInstanceId = agentInstanceId.trim();
    DeviceKey deviceKey =
        deviceKeys.requireByDeviceIdAndAgentInstanceIdAndStatus(
            deviceId,
            normalizedAgentInstanceId,
            DeviceKeyStatus.ACTIVE,
            SimpleGuardErrorCode.DEVICE_CREDENTIAL_INVALID,
            SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_INVALID);

    if (!signatureVerifier.verifyUnpairing(
        deviceKey.getPublicKey(), deviceId, normalizedAgentInstanceId, signature)) {
      throw invalidCredential();
    }

    return createPendingRequest(deviceId, normalizedAgentInstanceId);
  }

  @Transactional(readOnly = true)
  public AgentPairingStatusResponse statusForAgent(
      UUID deviceId, String agentInstanceId, String signature) {
    String normalizedAgentInstanceId = agentInstanceId.trim();
    DeviceKey deviceKey =
        deviceKeys.requireForAgentPairingStatus(deviceId, normalizedAgentInstanceId);

    if (!signatureVerifier.verifyUnpairing(
        deviceKey.getPublicKey(), deviceId, normalizedAgentInstanceId, signature)) {
      throw invalidCredential();
    }

    Device device = deviceService.findDeviceById(deviceId);
    String unpairingStatus =
        requests
            .findFirstByDeviceIdAndAgentInstanceIdOrderByRequestedAtDesc(
                deviceId, normalizedAgentInstanceId)
            .map(request -> request.getStatus().apiValue())
            .orElse(null);

    return new AgentPairingStatusResponse(
        deviceId, device.getPairingStatus().apiValue(), unpairingStatus);
  }

  private DeviceUnpairingRequestResponse createPendingRequest(
      UUID deviceId, String agentInstanceId) {
    Device device = deviceService.findDeviceById(deviceId);

    DeviceUnpairingRequest request =
        requests
            .findFirstByDeviceIdAndStatusOrderByRequestedAtDesc(
                deviceId, DeviceUnpairingRequestStatus.PENDING)
            .orElseGet(
                () ->
                    requests.saveAndFlush(
                        DeviceUnpairingRequest.pending(
                            UUID.randomUUID(),
                            device,
                            agentInstanceId,
                            OffsetDateTime.now(clock))));

    return DeviceUnpairingRequestResponse.from(request, device);
  }

  @Transactional(readOnly = true)
  public List<DeviceUnpairingRequestResponse> listPending(Account account) {
    return requests
        .findAllByAccountIdAndStatusOrderByRequestedAtDesc(
            account.getId(), DeviceUnpairingRequestStatus.PENDING)
        .stream()
        .map(
            request ->
                DeviceUnpairingRequestResponse.from(
                    request,
                    deviceService.findByIdAndAccountId(request.getDeviceId(), account.getId())))
        .toList();
  }

  @Transactional
  public DeviceUnpairingDecisionResponse decide(
      UUID requestId, Account account, DeviceUnpairingRequestStatus decision) {
    UnpairDeviceResponse unpairing = null;

    DeviceUnpairingRequest request = findPendingRequest(requestId, account);
    Device device = deviceService.findByIdAndAccountId(request.getDeviceId(), account.getId());

    request.respondeUnpairingRequest(account.getSubject(), OffsetDateTime.now(clock), decision);
    requests.saveAndFlush(request);

    if (decision == DeviceUnpairingRequestStatus.APPROVED) {
      unpairing = deviceService.unpairApproved(device, account.getSubject());
    }

    return new DeviceUnpairingDecisionResponse(
        DeviceUnpairingRequestResponse.from(request, device), unpairing);
  }

  private DeviceUnpairingRequest findPendingRequest(UUID requestId, Account account) {
    return requests
        .findByIdAndAccountIdAndStatus(
            requestId, account.getId(), DeviceUnpairingRequestStatus.PENDING)
        .orElseThrow(
            () ->
                new SimpleGuardException(
                    HttpStatus.NOT_FOUND,
                    SimpleGuardErrorCode.DEVICE_UNPAIRING_REQUEST_NOT_FOUND,
                    SimpleGuardTranslation.ERROR_DEVICE_UNPAIRING_REQUEST_NOT_FOUND));
  }

  private SimpleGuardException invalidCredential() {
    return new SimpleGuardException(
        HttpStatus.UNAUTHORIZED,
        SimpleGuardErrorCode.DEVICE_CREDENTIAL_INVALID,
        SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_INVALID);
  }
}
