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
    val failureReason: String?,
    val locationStatus: LocationCollectionStatus?,
    val batteryLevelPercentage: Int?,
    val batteryCharging: Boolean?,
    val networkType: NetworkType?,
    val signalStrengthDbm: Int?,
    val fineLocationPermission: PermissionState?,
    val coarseLocationPermission: PermissionState?
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

    fun recordSendSuccess(envelope: TelemetryEnvelope, at: Instant = Instant.now()) {
        val editor = preferences.edit()
            .putLong(KEY_LAST_SUCCESS_AT, at.toEpochMilli())
            .putString(KEY_STATUS, LocationDiagnosticStatus.SENT.name)
            .putString(KEY_PROVIDER, envelope.location?.provider)
            .putString(KEY_LOCATION_STATUS, envelope.locationStatus.name)
            .remove(KEY_FAILURE_REASON)
        putTechnical(editor, envelope.technical).apply()
    }

    fun recordSendFailure(envelope: TelemetryEnvelope, reason: String) {
        val editor = preferences.edit()
            .putString(KEY_STATUS, LocationDiagnosticStatus.SEND_FAILURE.name)
            .putString(KEY_PROVIDER, envelope.location?.provider)
            .putString(KEY_LOCATION_STATUS, envelope.locationStatus.name)
            .putString(KEY_FAILURE_REASON, reason)
        putTechnical(editor, envelope.technical).apply()
    }

    fun recordSendFailure(reason: String) {
        updateStatus(LocationDiagnosticStatus.SEND_FAILURE, provider = null, failureReason = reason)
    }

    fun snapshot(): LocationDiagnosticsSnapshot {
        return LocationDiagnosticsSnapshot(
            lastAttemptAt = preferences.instant(KEY_LAST_ATTEMPT_AT),
            lastSuccessAt = preferences.instant(KEY_LAST_SUCCESS_AT),
            status = preferences.getString(KEY_STATUS, LocationDiagnosticStatus.IDLE.name)
                ?.let { runCatching { LocationDiagnosticStatus.valueOf(it) }.getOrNull() }
                ?: LocationDiagnosticStatus.IDLE,
            provider = preferences.getString(KEY_PROVIDER, null),
            failureReason = preferences.getString(KEY_FAILURE_REASON, null),
            locationStatus = preferences.enumValue<LocationCollectionStatus>(KEY_LOCATION_STATUS),
            batteryLevelPercentage = preferences.intValue(KEY_BATTERY_LEVEL),
            batteryCharging = preferences.booleanValue(KEY_BATTERY_CHARGING),
            networkType = preferences.enumValue<NetworkType>(KEY_NETWORK_TYPE),
            signalStrengthDbm = preferences.intValue(KEY_SIGNAL_STRENGTH),
            fineLocationPermission = preferences.enumValue<PermissionState>(KEY_FINE_LOCATION_PERMISSION),
            coarseLocationPermission = preferences.enumValue<PermissionState>(KEY_COARSE_LOCATION_PERMISSION)
        )
    }

    private fun putTechnical(
        editor: android.content.SharedPreferences.Editor,
        technical: TechnicalTelemetryReading
    ): android.content.SharedPreferences.Editor {
        editor.putNullableInt(KEY_BATTERY_LEVEL, technical.batteryLevelPercentage)
        editor.putNullableBoolean(KEY_BATTERY_CHARGING, technical.batteryCharging)
        editor.putString(KEY_NETWORK_TYPE, technical.networkType?.name)
        editor.putNullableInt(KEY_SIGNAL_STRENGTH, technical.signalStrengthDbm)
        editor.putString(KEY_FINE_LOCATION_PERMISSION, technical.permissions?.fineLocation?.name)
        editor.putString(KEY_COARSE_LOCATION_PERMISSION, technical.permissions?.coarseLocation?.name)
        return editor
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

    private fun android.content.SharedPreferences.intValue(key: String): Int? {
        return if (contains(key)) getInt(key, 0) else null
    }

    private fun android.content.SharedPreferences.booleanValue(key: String): Boolean? {
        return if (contains(key)) getBoolean(key, false) else null
    }

    private inline fun <reified T : Enum<T>> android.content.SharedPreferences.enumValue(key: String): T? {
        return getString(key, null)?.let { runCatching { enumValueOf<T>(it) }.getOrNull() }
    }

    private fun android.content.SharedPreferences.Editor.putNullableInt(
        key: String,
        value: Int?
    ): android.content.SharedPreferences.Editor {
        return if (value == null) remove(key) else putInt(key, value)
    }

    private fun android.content.SharedPreferences.Editor.putNullableBoolean(
        key: String,
        value: Boolean?
    ): android.content.SharedPreferences.Editor {
        return if (value == null) remove(key) else putBoolean(key, value)
    }

    private companion object {
        const val PREFERENCES_NAME = "simpleguard-location-diagnostics"
        const val KEY_LAST_ATTEMPT_AT = "last_attempt_at"
        const val KEY_LAST_SUCCESS_AT = "last_success_at"
        const val KEY_STATUS = "status"
        const val KEY_PROVIDER = "provider"
        const val KEY_FAILURE_REASON = "failure_reason"
        const val KEY_LOCATION_STATUS = "location_status"
        const val KEY_BATTERY_LEVEL = "battery_level"
        const val KEY_BATTERY_CHARGING = "battery_charging"
        const val KEY_NETWORK_TYPE = "network_type"
        const val KEY_SIGNAL_STRENGTH = "signal_strength"
        const val KEY_FINE_LOCATION_PERMISSION = "fine_location_permission"
        const val KEY_COARSE_LOCATION_PERMISSION = "coarse_location_permission"
    }
}
