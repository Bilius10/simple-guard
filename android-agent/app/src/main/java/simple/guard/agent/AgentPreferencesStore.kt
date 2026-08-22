package simple.guard.agent

import android.app.Activity
import android.provider.Settings

internal class AgentPreferencesStore(
    private val activity: Activity,
) {
    fun loadLocalPairing(): LocalPairing? {
        val preferences = activity.getPreferences(Activity.MODE_PRIVATE)
        val pendingDeviceId = preferences.getString(PENDING_UNPAIR_DEVICE_ID_KEY, null)
        if (!pendingDeviceId.isNullOrBlank()) {
            return LocalPairing(
                deviceId = pendingDeviceId,
                deviceName = preferences.getString(PENDING_UNPAIR_DEVICE_NAME_KEY, DEFAULT_DEVICE_NAME) ?: DEFAULT_DEVICE_NAME,
                instanceUrl = preferences.getString(PENDING_UNPAIR_INSTANCE_URL_KEY, "").orEmpty(),
                pendingSynchronization = true,
            )
        }

        val pairedDeviceId = preferences.getString(PAIRED_DEVICE_ID_KEY, null)
        if (pairedDeviceId.isNullOrBlank()) {
            return null
        }

        return LocalPairing(
            deviceId = pairedDeviceId,
            deviceName = preferences.getString(PAIRED_DEVICE_NAME_KEY, DEFAULT_DEVICE_NAME) ?: DEFAULT_DEVICE_NAME,
            instanceUrl = preferences.getString(PAIRED_INSTANCE_URL_KEY, "").orEmpty(),
            pendingSynchronization = false,
        )
    }

    fun persistPairing(pairing: LocalPairing) {
        activity.getPreferences(Activity.MODE_PRIVATE).edit()
            .putString(PAIRED_DEVICE_ID_KEY, pairing.deviceId)
            .putString(PAIRED_DEVICE_NAME_KEY, pairing.deviceName)
            .putString(PAIRED_INSTANCE_URL_KEY, pairing.instanceUrl)
            .remove(PENDING_UNPAIR_DEVICE_ID_KEY)
            .remove(PENDING_UNPAIR_DEVICE_NAME_KEY)
            .remove(PENDING_UNPAIR_INSTANCE_URL_KEY)
            .apply()
    }

    fun persistPendingUnpairing(pairing: LocalPairing) {
        activity.getPreferences(Activity.MODE_PRIVATE).edit()
            .putString(PENDING_UNPAIR_DEVICE_ID_KEY, pairing.deviceId)
            .putString(PENDING_UNPAIR_DEVICE_NAME_KEY, pairing.deviceName)
            .putString(PENDING_UNPAIR_INSTANCE_URL_KEY, pairing.instanceUrl)
            .remove(PAIRED_DEVICE_ID_KEY)
            .remove(PAIRED_DEVICE_NAME_KEY)
            .remove(PAIRED_INSTANCE_URL_KEY)
            .apply()
    }

    fun clearLocalPairing() {
        activity.getPreferences(Activity.MODE_PRIVATE).edit()
            .remove(PAIRED_DEVICE_ID_KEY)
            .remove(PAIRED_DEVICE_NAME_KEY)
            .remove(PAIRED_INSTANCE_URL_KEY)
            .remove(PENDING_UNPAIR_DEVICE_ID_KEY)
            .remove(PENDING_UNPAIR_DEVICE_NAME_KEY)
            .remove(PENDING_UNPAIR_INSTANCE_URL_KEY)
            .apply()
    }

    fun agentInstanceId(): String {
        val preferences = activity.getPreferences(Activity.MODE_PRIVATE)
        val current = preferences.getString(AGENT_INSTANCE_ID_KEY, null)
        if (!current.isNullOrBlank()) {
            return current
        }

        val androidId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
        val created = "android-${androidId ?: System.currentTimeMillis().toString(16)}"
        preferences.edit().putString(AGENT_INSTANCE_ID_KEY, created).apply()
        return created
    }

    private companion object {
        const val DEFAULT_DEVICE_NAME = "Dispositivo Android"
        const val AGENT_INSTANCE_ID_KEY = "agent_instance_id"
        const val PAIRED_DEVICE_ID_KEY = "paired_device_id"
        const val PAIRED_DEVICE_NAME_KEY = "paired_device_name"
        const val PAIRED_INSTANCE_URL_KEY = "paired_instance_url"
        const val PENDING_UNPAIR_DEVICE_ID_KEY = "pending_unpair_device_id"
        const val PENDING_UNPAIR_DEVICE_NAME_KEY = "pending_unpair_device_name"
        const val PENDING_UNPAIR_INSTANCE_URL_KEY = "pending_unpair_instance_url"
    }
}
