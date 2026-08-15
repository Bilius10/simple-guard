package simple.guard.api.devices.pairingsession.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import simple.guard.api.devices.pairingsession.service.PairingSessionService;

@Component
public class PairingSessionScheduler {

    private final PairingSessionService pairingSessionService;

    public PairingSessionScheduler(PairingSessionService pairingSessionService) {
        this.pairingSessionService = pairingSessionService;
    }

    @Scheduled(fixedDelayString = "${simpleguard.pairing.expiration-scan-interval:PT30S}")
    public void expireElapsedSessions() {
        pairingSessionService.expireElapsedSessions();
    }
}


