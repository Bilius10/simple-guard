package simple.guard.agent.location

import android.content.Context
import java.time.Instant

enum class LocationDiagnosticStatus(val label: String) {
    IDLE("Aguardando coleta"),
    SENT("Enviado com sucesso"),
    SEND_FAILURE("Falha ao enviar"),
    LOCATION_UNAVAILABLE("Localizacao indisponivel"),
    PROVIDER_UNAVAILABLE("Provedor indisponivel"),
    PERMISSION_DENIED("Permissao negada")
}

data class LocationDiagnosticsSnapshot(
    val lastAttemptAt: Instant?,
    val lastSuccessAt: Instant?,
    val status: LocationDiagnosticStatus,
    val provider: String?,
    val failureReason: String?
)

class LocationDiagnosticsStore(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun recordSyncAttempt(at: Instant = Instant.now()) {
        preferences.edit()
            .putLong(KEY_LAST_ATTEMPT_AT, at.toEpochMilli())
            .apply()
    }

    fun recordPermissionDenied(reason: String) {
        updateStatus(LocationDiagnosticStatus.PERMISSION_DENIED, provider = null, failureReason = reason)
    }

    fun recordProviderUnavailable(reason: String) {
        updateStatus(LocationDiagnosticStatus.PROVIDER_UNAVAILABLE, provider = null, failureReason = reason)
    }

    fun recordLocationUnavailable(provider: String?, reason: String) {
        updateStatus(LocationDiagnosticStatus.LOCATION_UNAVAILABLE, provider = provider, failureReason = reason)
    }

    fun recordSendSuccess(provider: String, at: Instant = Instant.now()) {
        preferences.edit()
            .putLong(KEY_LAST_SUCCESS_AT, at.toEpochMilli())
            .putString(KEY_STATUS, LocationDiagnosticStatus.SENT.name)
            .putString(KEY_PROVIDER, provider)
            .remove(KEY_FAILURE_REASON)
            .apply()
    }

    fun recordSendFailure(provider: String, reason: String) {
        updateStatus(LocationDiagnosticStatus.SEND_FAILURE, provider = provider, failureReason = reason)
    }

    fun snapshot(): LocationDiagnosticsSnapshot {
        return LocationDiagnosticsSnapshot(
            lastAttemptAt = preferences.instant(KEY_LAST_ATTEMPT_AT),
            lastSuccessAt = preferences.instant(KEY_LAST_SUCCESS_AT),
            status = preferences.getString(KEY_STATUS, LocationDiagnosticStatus.IDLE.name)
                ?.let { runCatching { LocationDiagnosticStatus.valueOf(it) }.getOrNull() }
                ?: LocationDiagnosticStatus.IDLE,
            provider = preferences.getString(KEY_PROVIDER, null),
            failureReason = preferences.getString(KEY_FAILURE_REASON, null)
        )
    }

    private fun updateStatus(
        status: LocationDiagnosticStatus,
        provider: String?,
        failureReason: String?
    ) {
        preferences.edit()
            .putString(KEY_STATUS, status.name)
            .putString(KEY_PROVIDER, provider)
            .putString(KEY_FAILURE_REASON, failureReason)
            .apply()
    }

    private fun android.content.SharedPreferences.instant(key: String): Instant? {
        return if (contains(key)) Instant.ofEpochMilli(getLong(key, 0L)) else null
    }

    private companion object {
        const val PREFERENCES_NAME = "simpleguard-location-diagnostics"
        const val KEY_LAST_ATTEMPT_AT = "last_attempt_at"
        const val KEY_LAST_SUCCESS_AT = "last_success_at"
        const val KEY_STATUS = "status"
        const val KEY_PROVIDER = "provider"
        const val KEY_FAILURE_REASON = "failure_reason"
    }
}
