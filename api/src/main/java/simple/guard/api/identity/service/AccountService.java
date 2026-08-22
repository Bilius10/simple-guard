package simple.guard.api.identity.service;

import org.springframework.stereotype.Service;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.identity.domain.AccountRepository;

import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accounts;

    public AccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public Optional<Account> findActiveBySubject(String subject) {
        return accounts.findBySubjectAndActive(subject, true);
    }
}
