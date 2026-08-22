package simple.guard.api.devices.deviceunpairingrequest.controller.response;

import simple.guard.api.devices.device.controller.response.UnpairDeviceResponse;

public record DeviceUnpairingDecisionResponse(
    DeviceUnpairingRequestResponse request, UnpairDeviceResponse unpairing) {}
