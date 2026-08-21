package simple.guard.agent.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.telephony.TelephonyManager
import java.time.Instant

class AndroidTechnicalTelemetryCollector(context: Context) : TechnicalTelemetryCollector {

    private val applicationContext = context.applicationContext

    override fun collect(): TechnicalTelemetryReading {
        val battery = batteryState()
        val networkType = networkType()
        return TechnicalTelemetryReading(
            batteryLevelPercentage = battery?.first,
            batteryCharging = battery?.second,
            networkType = networkType,
            signalStrengthDbm = signalStrengthDbm(networkType),
            permissions = TelemetryPermissions(
                fineLocation = permissionState(Manifest.permission.ACCESS_FINE_LOCATION),
                coarseLocation = permissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
            ),
            collectedAt = Instant.now()
        )
    }

    private fun batteryState(): Pair<Int?, Boolean?>? {
        val state = applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return null
        val level = state.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = state.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = TechnicalTelemetryValueNormalizer.batteryPercentage(level, scale)
        val charging = when (state.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING,
            BatteryManager.BATTERY_STATUS_FULL -> true
            BatteryManager.BATTERY_STATUS_DISCHARGING,
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
            else -> null
        }
        return percentage to charging
    }

    private fun networkType(): NetworkType? {
        return runCatching {
            val manager = applicationContext.getSystemService(ConnectivityManager::class.java)
            val network = manager.activeNetwork ?: return NetworkType.NONE
            val capabilities = manager.getNetworkCapabilities(network) ?: return null
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }
        }.getOrNull()
    }

    private fun signalStrengthDbm(networkType: NetworkType?): Int? {
        val value = runCatching {
            when (networkType) {
                NetworkType.WIFI -> applicationContext.getSystemService(WifiManager::class.java)
                    .connectionInfo
                    .rssi
                NetworkType.CELLULAR -> cellularSignalStrengthDbm()
                else -> null
            }
        }.getOrNull()
        return TechnicalTelemetryValueNormalizer.signalStrengthDbm(value)
    }

    private fun cellularSignalStrengthDbm(): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null
        }
        return applicationContext.getSystemService(TelephonyManager::class.java)
            .signalStrength
            ?.cellSignalStrengths
            ?.map { it.dbm }
            ?.filter { it in -160..0 }
            ?.maxOrNull()
    }

    private fun permissionState(permission: String): PermissionState {
        return if (applicationContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
    }
}
