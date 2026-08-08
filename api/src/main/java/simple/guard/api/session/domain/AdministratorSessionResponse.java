package simple.guard.api.session.domain;

public record AdministratorSessionResponse(
        String subject,
        String email,
        String displayName,
        String role
) {
}
