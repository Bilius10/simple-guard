package simple.guard.api.shared.i18n;

public enum SimpleGuardTranslation {
    ERROR_INVALID_TOKEN("simple_guard_error_invalid_token"),
    ERROR_ACCESS_DENIED("simple_guard_error_access_denied"),
    ERROR_VALIDATION("simple_guard_error_validation"),
    ERROR_SYSTEM("simple_guard_error_system"),
    INSTANCE_ID_REQUIRED("simple_guard_instance_id_required"),
    PUBLIC_URL_REQUIRED("simple_guard_public_url_required"),
    PUBLIC_URL_ABSOLUTE_REQUIRED("simple_guard_public_url_absolute_required"),
    OIDC_ISSUER_URI_REQUIRED("simple_guard_oidc_issuer_uri_required"),
    OIDC_JWK_SET_URI_REQUIRED("simple_guard_oidc_jwk_set_uri_required"),
    ERROR_ACCOUNT_NOT_FOUND("simple_guard_error_account_not_found");

    private final String key;

    SimpleGuardTranslation(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
