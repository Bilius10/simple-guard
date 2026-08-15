package simple.guard.api.identity.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.identity.service.AccountService;

import java.util.List;

import static simple.guard.api.shared.i18n.SimpleGuardTranslation.ERROR_ACCOUNT_NOT_FOUND;

@Component
public class AccountJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private final AccountService accounts;

    public AccountJwtAuthenticationConverter(AccountService accounts) {
        this.accounts = accounts;
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        Account account = accounts.findActiveBySubject(jwt.getSubject())
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN),
                        ERROR_ACCOUNT_NOT_FOUND.name()
                ));

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole())),
                account.getSubject()
        );
        authentication.setDetails(account);
        return authentication;
    }
}
