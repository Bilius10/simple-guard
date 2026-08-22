package simple.guard.api.shared.i18n;

public enum SimpleGuardTranslation {
    ERROR_INVALID_TOKEN("simple_guard_error_invalid_token"),
    ERROR_ACCESS_DENIED("simple_guard_error_access_denied"),
    ERROR_CRITICAL_ACTION_CONFIRMATION_REQUIRED("simple_guard_error_critical_action_confirmation_required"),
    ERROR_DEVICE_NOT_FOUND("simple_guard_error_device_not_found"),
    ERROR_DEVICE_ALREADY_PAIRED("simple_guard_error_device_already_paired"),
    ERROR_DEVICE_PLATFORM_MISMATCH("simple_guard_error_device_platform_mismatch"),
    ERROR_DEVICE_CREDENTIAL_INVALID("simple_guard_error_device_credential_invalid"),
    ERROR_DEVICE_CREDENTIAL_REVOKED("simple_guard_error_device_credential_revoked"),
    ERROR_TELEMETRY_BATCH_ITEM_REQUIRED("simple_guard_error_telemetry_batch_item_required"),
    ERROR_TELEMETRY_BATCH_ITEM_INVALID("simple_guard_error_telemetry_batch_item_invalid"),
    ERROR_DEVICE_UNPAIRING_REQUEST_NOT_FOUND("simple_guard_error_device_unpairing_request_not_found"),
    ERROR_PAIRING_SESSION_INVALID("simple_guard_error_pairing_session_invalid"),
    ERROR_PAIRING_SESSION_EXPIRED("simple_guard_error_pairing_session_expired"),
    ERROR_PAIRING_SESSION_ALREADY_USED("simple_guard_error_pairing_session_already_used"),
    ERROR_VALIDATION("simple_guard_error_validation"),
    ERROR_SYSTEM("simple_guard_error_system"),
    INSTANCE_ID_REQUIRED("simple_guard_instance_id_required"),
    PUBLIC_URL_REQUIRED("simple_guard_public_url_required"),
    PUBLIC_URL_ABSOLUTE_REQUIRED("simple_guard_public_url_absolute_required"),
    OIDC_ISSUER_URI_REQUIRED("simple_guard_oidc_issuer_uri_required"),
    OIDC_JWK_SET_URI_REQUIRED("simple_guard_oidc_jwk_set_uri_required"),
    ERROR_ACCOUNT_NOT_FOUND("simple_guard_error_account_not_found"),
    ERROR_SHA_256_NOT_AVAILABLE("simple_guard_error_SHA_256_not_available"),
    ERROR_MAX_OPEN_PAIRING_SESSIONS_REACHED("simple_guard_error_max_open_pairing_sessions_reached"),
    INVALID_STATUS_MESSAGE("simple_guard_unpairing_decision_invalid");

    private final String key;

    SimpleGuardTranslation(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
