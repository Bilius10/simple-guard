package simple.guard.api.session.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.session.domain.AdministratorSessionResponse;

@RestController
@RequestMapping("/api/session")
public class AdministratorSessionController {

    @GetMapping("/me")
    AdministratorSessionResponse me(Authentication authentication) {
        Account account = (Account) authentication.getDetails();
        return new AdministratorSessionResponse(
                account.getSubject(),
                account.getEmail(),
                account.getDisplayName(),
                account.getRole()
        );
    }
}
