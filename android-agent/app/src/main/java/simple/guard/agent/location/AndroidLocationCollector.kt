package simple.guard.agent.location

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

class AndroidLocationCollector(context: Context) : LocationCollector {

    private val applicationContext = context.applicationContext
    private val locationManager = applicationContext.getSystemService(LocationManager::class.java)
    private val diagnosticsStore = LocationDiagnosticsStore(applicationContext)

    override fun collect(callback: (LocationCollectionResult) -> Unit) {
        if (!hasLocationPermission()) {
            val reason = "Permissao de localizacao nao concedida."
            Log.w(TAG, "Coleta de localizacao bloqueada: permissao negada.")
            diagnosticsStore.recordPermissionDenied(reason)
            callback(LocationCollectionResult.PermissionDenied)
            return
        }

        val providers = enabledProviders()
        if (providers.isEmpty()) {
            val reason = "Nenhum provedor GPS/FUSED/NETWORK habilitado."
            Log.w(TAG, "Coleta de localizacao bloqueada: nenhum provedor GPS/FUSED/NETWORK habilitado.")
            diagnosticsStore.recordProviderUnavailable(reason)
            callback(LocationCollectionResult.ProviderUnavailable)
            return
        }

        try {
            Log.i(TAG, "Iniciando coleta de localizacao com provedores: ${providers.joinToString { it.contractName }}.")
            requestCurrentLocation(providers, 0, callback)
        } catch (_: SecurityException) {
            val reason = "Permissao de localizacao revogada durante a coleta."
            Log.w(TAG, "Coleta de localizacao bloqueada apos iniciar: permissao negada.")
            diagnosticsStore.recordPermissionDenied(reason)
            callback(LocationCollectionResult.PermissionDenied)
        } catch (_: IllegalArgumentException) {
            val reason = "Provedores configurados ficaram indisponiveis durante a coleta."
            Log.w(TAG, "Coleta de localizacao falhou: provedores configurados ficaram indisponiveis.")
            diagnosticsStore.recordProviderUnavailable(reason)
            callback(LocationCollectionResult.ProviderUnavailable)
        } catch (_: RuntimeException) {
            val reason = "Falha inesperada durante a coleta de localizacao."
            Log.w(TAG, "Coleta de localizacao falhou antes de retornar ponto valido.")
            diagnosticsStore.recordLocationUnavailable(provider = null, reason = reason)
            callback(LocationCollectionResult.LocationUnavailable)
        }
    }

    private fun requestCurrentLocation(
        providers: List<LocationProviderCandidate>,
        index: Int,
        callback: (LocationCollectionResult) -> Unit
    ) {
        if (index >= providers.size) {
            callback(LocationCollectionResult.LocationUnavailable)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestModernLocation(providers, index, callback)
            return
        }

        requestLegacyLocation(providers, index, callback)
    }

    @TargetApi(Build.VERSION_CODES.R)
    private fun requestModernLocation(
        providers: List<LocationProviderCandidate>,
        index: Int,
        callback: (LocationCollectionResult) -> Unit
    ) {
        val provider = providers[index]
        val handler = Handler(Looper.getMainLooper())
        val delivered = AtomicBoolean(false)
        val cancellation = CancellationSignal()
        val timeout = Runnable {
            if (delivered.compareAndSet(false, true)) {
                cancellation.cancel()
                continueWithFallback(
                    providers,
                    index,
                    callback,
                    provider,
                    "tempo limite de ${LOCATION_TIMEOUT_MS / 1000}s excedido"
                )
            }
        }
        handler.postDelayed(timeout, LOCATION_TIMEOUT_MS)
        try {
            Log.i(TAG, "Tentando coletar localizacao via ${provider.contractName}.")
            locationManager.getCurrentLocation(
                provider.systemName,
                cancellation,
                applicationContext.mainExecutor
            ) { location ->
                if (delivered.compareAndSet(false, true)) {
                    handler.removeCallbacks(timeout)
                    location?.let {
                        Log.i(TAG, "Localizacao obtida via ${provider.contractName}.")
                        callback(LocationCollectionResult.Collected(it.toReading(provider.contractName)))
                    } ?: continueWithFallback(
                        providers,
                        index,
                        callback,
                        provider,
                        "provedor retornou localizacao nula"
                    )
                }
            }
        } catch (_: IllegalArgumentException) {
            handler.removeCallbacks(timeout)
            continueWithFallback(providers, index, callback, provider, "provedor nao suportado neste dispositivo")
        } catch (exception: SecurityException) {
            handler.removeCallbacks(timeout)
            throw exception
        } catch (exception: RuntimeException) {
            handler.removeCallbacks(timeout)
            continueWithFallback(providers, index, callback, provider, exception.javaClass.simpleName)
        }
    }

    private fun requestLegacyLocation(
        providers: List<LocationProviderCandidate>,
        index: Int,
        callback: (LocationCollectionResult) -> Unit
    ) {
        val provider = providers[index]
        val handler = Handler(Looper.getMainLooper())
        val delivered = AtomicBoolean(false)
        lateinit var timeout: Runnable
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (delivered.compareAndSet(false, true)) {
                    handler.removeCallbacks(timeout)
                    locationManager.removeUpdates(this)
                    Log.i(TAG, "Localizacao obtida via ${provider.contractName}.")
                    callback(LocationCollectionResult.Collected(location.toReading(provider.contractName)))
                }
            }

            override fun onProviderDisabled(disabledProvider: String) {
                if (delivered.compareAndSet(false, true)) {
                    handler.removeCallbacks(timeout)
                    locationManager.removeUpdates(this)
                    continueWithFallback(
                        providers,
                        index,
                        callback,
                        provider,
                        "provedor ${disabledProvider.uppercase()} foi desabilitado durante a coleta"
                    )
                }
            }

            @Deprecated("Required on Android 8 and 9")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }
        timeout = Runnable {
            if (delivered.compareAndSet(false, true)) {
                locationManager.removeUpdates(listener)
                continueWithFallback(
                    providers,
                    index,
                    callback,
                    provider,
                    "tempo limite de ${LOCATION_TIMEOUT_MS / 1000}s excedido"
                )
            }
        }
        handler.postDelayed(timeout, LOCATION_TIMEOUT_MS)
        try {
            Log.i(TAG, "Tentando coletar localizacao via ${provider.contractName}.")
            locationManager.requestSingleUpdate(
                provider.systemName,
                listener,
                Looper.getMainLooper()
            )
        } catch (_: IllegalArgumentException) {
            handler.removeCallbacks(timeout)
            continueWithFallback(providers, index, callback, provider, "provedor nao suportado neste dispositivo")
        } catch (exception: SecurityException) {
            handler.removeCallbacks(timeout)
            throw exception
        } catch (exception: RuntimeException) {
            handler.removeCallbacks(timeout)
            continueWithFallback(providers, index, callback, provider, exception.javaClass.simpleName)
        }
    }

    private fun continueWithFallback(
        providers: List<LocationProviderCandidate>,
        index: Int,
        callback: (LocationCollectionResult) -> Unit,
        provider: LocationProviderCandidate,
        reason: String
    ) {
        val nextIndex = index + 1
        if (nextIndex < providers.size) {
            Log.w(
                TAG,
                "Provedor ${provider.contractName} indisponivel: $reason. Tentando ${providers[nextIndex].contractName}."
            )
            requestCurrentLocation(providers, nextIndex, callback)
            return
        }

        Log.w(TAG, "Todos os provedores falharam. Ultimo provedor ${provider.contractName}: $reason.")
        diagnosticsStore.recordLocationUnavailable(provider.contractName, reason)
        callback(LocationCollectionResult.LocationUnavailable)
    }

    private fun enabledProviders(): List<LocationProviderCandidate> {
        return LocationProviderPolicy.preferredProviders(
            enabledProviderNames(),
            precisePermissionGranted = hasFineLocationPermission()
        )
    }

    private fun enabledProviderNames(): Set<String> {
        return LocationProviderPolicy.supportedProviders()
            .mapTo(LinkedHashSet()) { it.systemName }
            .filterTo(LinkedHashSet()) { providerEnabled(it) }
    }

    private fun providerEnabled(providerName: String): Boolean {
        return runCatching { locationManager.isProviderEnabled(providerName) }.getOrDefault(false)
    }

    private fun hasLocationPermission(): Boolean {
        return hasFineLocationPermission() || hasCoarseLocationPermission()
    }

    private fun hasFineLocationPermission(): Boolean {
        return applicationContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun hasCoarseLocationPermission(): Boolean {
        return applicationContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun Location.toReading(providerName: String): LocationReading {
        return LocationReading(
            latitude = LocationValueNormalizer.latitude(latitude),
            longitude = LocationValueNormalizer.longitude(longitude),
            accuracyMeters = if (hasAccuracy()) {
                LocationValueNormalizer.accuracyMeters(accuracy.toDouble())
            } else {
                null
            },
            altitudeMeters = if (hasAltitude()) {
                LocationValueNormalizer.altitudeMeters(altitude)
            } else {
                null
            },
            speedMetersPerSecond = if (hasSpeed()) {
                LocationValueNormalizer.speedMetersPerSecond(speed.toDouble())
            } else {
                null
            },
            provider = providerName,
            collectedAt = Instant.ofEpochMilli(time)
        )
    }

    private companion object {
        const val TAG = "SimpleGuardLocation"
        const val LOCATION_TIMEOUT_MS = 30_000L
    }
}
