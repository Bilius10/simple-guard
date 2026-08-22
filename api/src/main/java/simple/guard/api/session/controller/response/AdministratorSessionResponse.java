package simple.guard.api.session.controller.response;

public record AdministratorSessionResponse(
    String subject, String email, String displayName, String role) {}
