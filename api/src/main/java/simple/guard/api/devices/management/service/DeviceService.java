package simple.guard.api.devices.management.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.devices.management.controller.CreateDeviceRequest;
import simple.guard.api.devices.management.controller.DeviceResponse;
import simple.guard.api.devices.management.domain.Device;
import simple.guard.api.devices.management.domain.DevicePairingStatus;
import simple.guard.api.devices.management.domain.DeviceRepository;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository devices;

    public DeviceService(DeviceRepository devices) {
        this.devices = devices;
    }

    @Transactional
    public DeviceResponse create(CreateDeviceRequest request, Account account) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Device device = new Device(
                UUID.randomUUID(),
                account.getId(),
                request.name().trim(),
                request.type(),
                request.platform(),
                DevicePairingStatus.UNPAIRED,
                account.getSubject(),
                now,
                account.getSubject(),
                now
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
}
