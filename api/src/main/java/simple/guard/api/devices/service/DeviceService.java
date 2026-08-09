package simple.guard.api.devices.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.devices.controller.CreateDeviceRequest;
import simple.guard.api.devices.controller.DeviceResponse;
import simple.guard.api.devices.domain.Device;
import simple.guard.api.devices.domain.DevicePairingStatus;
import simple.guard.api.devices.domain.DeviceRepository;
import simple.guard.api.identity.domain.Account;

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
}
