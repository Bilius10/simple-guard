package simple.guard.api.devices.device.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.devices.device.controller.response.UnpairDeviceResponse;
import simple.guard.api.devices.device.controller.request.CreateDeviceRequest;
import simple.guard.api.devices.device.controller.response.DeviceResponse;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.domain.DevicePairingStatus;
import simple.guard.api.devices.device.domain.DeviceRepository;
import simple.guard.api.devices.devicekey.service.DeviceKeyService;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository devices;
    private final DeviceKeyService deviceKeys;
    private final Clock clock;

    public DeviceService(DeviceRepository devices, DeviceKeyService deviceKeys, Clock clock) {
        this.devices = devices;
        this.deviceKeys = deviceKeys;
        this.clock = clock;
    }

    @Transactional
    public DeviceResponse create(CreateDeviceRequest request, Account account) {
        Device device = new Device(
                UUID.randomUUID(),
                account.getId(),
                request.name().trim(),
                request.type(),
                request.platform(),
                DevicePairingStatus.UNPAIRED
        );

        return DeviceResponse.from(devices.save(device));
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> list(Account account) {
        return devices.findAllByAccountIdOrderByCreatedAtDesc(account.getId()).stream()
                .map(DeviceResponse::from)
                .toList();
    }

    public Device findByIdAndAccountId(UUID deviceId, UUID accountId) {
        return devices.findByIdAndAccountId(deviceId, accountId)
                .orElseThrow(() -> new SimpleGuardException(
                        HttpStatus.NOT_FOUND,
                        SimpleGuardErrorCode.DEVICE_NOT_FOUND,
                        SimpleGuardTranslation.ERROR_DEVICE_NOT_FOUND
                ));
    }

    public Device findDeviceById(UUID deviceId) {
        return devices.findById(deviceId)
                .orElseThrow(() -> new SimpleGuardException(
                        HttpStatus.NOT_FOUND,
                        SimpleGuardErrorCode.DEVICE_NOT_FOUND,
                        SimpleGuardTranslation.ERROR_DEVICE_NOT_FOUND
                ));
    }

    public void updatePairingStatus(Device device, DevicePairingStatus pairingStatus) {
        device.setPairingStatus(pairingStatus);
        devices.saveAndFlush(device);
    }

    @Transactional
    public UnpairDeviceResponse unpairByAdministrator(UUID deviceId, Account account) {
        Device device = findByIdAndAccountId(deviceId, account.getId());
        return unpairApproved(device, account.getSubject());
    }

    public UnpairDeviceResponse unpairApproved(Device device, String actor) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        int revokedKeyCount = deviceKeys.revokeActiveKeysForDevice(device.getId(), actor, now);

        if (device.getPairingStatus() != DevicePairingStatus.UNPAIRED) {
            updatePairingStatus(device, DevicePairingStatus.UNPAIRED);
        }

        return new UnpairDeviceResponse(
                device.getId(),
                DevicePairingStatus.UNPAIRED.apiValue(),
                revokedKeyCount,
                now
        );
    }

}

