package simple.guard.api.devices.pairingsession.controller;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.devices.pairingsession.controller.request.CompleteAgentPairingRequest;
import simple.guard.api.devices.pairingsession.controller.response.CompleteAgentPairingResponse;
import simple.guard.api.devices.pairingsession.service.PairingSessionService;

@RestController
@RequestMapping("/api/agent/pairing")
public class AgentPairingController {

    private final PairingSessionService pairingSessionService;

    public AgentPairingController(PairingSessionService pairingSessionService) {
        this.pairingSessionService = pairingSessionService;
    }

    @PostMapping("/complete")
    ResponseEntity<CompleteAgentPairingResponse> completePairing(
            @Valid @RequestBody CompleteAgentPairingRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(pairingSessionService.complete(request));
    }
}
