package simple.guard.api.devices.pairingsession.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.devices.pairingsession.controller.response.PairingSessionResponse;
import simple.guard.api.devices.pairingsession.service.PairingSessionService;
import simple.guard.api.identity.domain.Account;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class PairingSessionController {

    private final PairingSessionService pairingSessionService;

    public PairingSessionController(PairingSessionService pairingSessionService) {
        this.pairingSessionService = pairingSessionService;
    }

    @PostMapping("/{deviceId}/pairing-sessions")
    ResponseEntity<PairingSessionResponse> generatePairingSession(
            @PathVariable UUID deviceId,
            Authentication authentication
    ) {
        PairingSessionResponse session = pairingSessionService.generate(deviceId, account(authentication));
        return ResponseEntity.created(URI.create(
                        "/api/devices/" + deviceId + "/pairing-sessions/" + session.pairingSessionId()
                ))
                .cacheControl(CacheControl.noStore())
                .body(session);
    }

    private Account account(Authentication authentication) {
        return (Account) authentication.getDetails();
    }
}
