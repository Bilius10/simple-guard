package simple.guard.api.devices.pairing.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import simple.guard.api.devices.management.domain.DevicePlatform;

public record CompleteAgentPairingRequest(
        @NotBlank(message = "{simple_guard_pairing_code_required}")
        String pairingCode,

        @NotBlank(message = "{simple_guard_agent_instance_id_required}")
        @Size(max = 128, message = "{simple_guard_agent_instance_id_size}")
        String agentInstanceId,

        @NotNull(message = "{simple_guard_device_platform_required}")
        DevicePlatform platform,

        @NotBlank(message = "{simple_guard_agent_public_key_required}")
        @Size(min = 64, max = 4096, message = "{simple_guard_agent_public_key_size}")
        String publicKey
) {
}
