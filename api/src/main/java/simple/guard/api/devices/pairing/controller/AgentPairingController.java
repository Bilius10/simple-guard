package simple.guard.api.devices.pairing.controller;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.devices.pairing.controller.request.CompleteAgentPairingRequest;
import simple.guard.api.devices.pairing.controller.response.CompleteAgentPairingResponse;
import simple.guard.api.devices.pairing.service.AgentPairingService;

@RestController
@RequestMapping("/api/agent/pairing")
public class AgentPairingController {

    private final AgentPairingService agentPairingService;

    public AgentPairingController(AgentPairingService agentPairingService) {
        this.agentPairingService = agentPairingService;
    }

    @PostMapping("/complete")
    ResponseEntity<CompleteAgentPairingResponse> completePairing(
            @Valid @RequestBody CompleteAgentPairingRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(agentPairingService.complete(request));
    }
}
