package simple.guard.api.devices.deviceunpairingrequest.controller.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestStatus;

import static simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestStatus.APPROVED;
import static simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestStatus.REJECTED;

public record DecideDeviceUnpairingRequest(
        @NotNull(message = "{simple_guard_unpairing_decision_required}")
        DeviceUnpairingRequestStatus status
) {

    @AssertTrue(message = "{simple_guard_unpairing_decision_invalid}")
    public boolean isTerminalStatus() {
        return status == null || status == APPROVED || status == REJECTED;
    }
}


