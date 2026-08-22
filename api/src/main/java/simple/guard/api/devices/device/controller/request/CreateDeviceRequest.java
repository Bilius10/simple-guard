package simple.guard.api.devices.device.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import simple.guard.api.devices.device.domain.DevicePlatform;
import simple.guard.api.devices.device.domain.DeviceType;

public record CreateDeviceRequest(
    @NotBlank(message = "{simple_guard_device_name_required}")
        @Size(max = 160, message = "{simple_guard_device_name_size}")
        String name,
    @NotNull(message = "{simple_guard_device_type_required}") DeviceType type,
    @NotNull(message = "{simple_guard_device_platform_required}") DevicePlatform platform) {}
