package simple.guard.agent.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import simple.guard.agent.MainActivity
import simple.guard.agent.pairing.AgentKeyStore
import java.io.File
import java.time.Instant
import java.util.UUID

class LocationTrackingService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var synchronizationService: OfflineTelemetrySynchronizationService? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var synchronizationInProgress = false
    private lateinit var diagnosticsStore: LocationDiagnosticsStore

    override fun onCreate() {
        super.onCreate()
        diagnosticsStore = LocationDiagnosticsStore(this)
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val instanceUrl = intent?.getStringExtra(EXTRA_INSTANCE_URL)
        val deviceId = intent?.getStringExtra(EXTRA_DEVICE_ID)
        val agentInstanceId = intent?.getStringExtra(EXTRA_AGENT_INSTANCE_ID)
        if (instanceUrl.isNullOrBlank() || deviceId.isNullOrBlank() || agentInstanceId.isNullOrBlank()) {
            Log.w(TAG, "Servico de localizacao recebeu parametros incompletos e sera interrompido.")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.i(TAG, "Servico de telemetria iniciado para dispositivo pareado.")
        startAsForeground("Preparando coleta de telemetria.")
        handler.removeCallbacksAndMessages(null)
        unregisterNetworkCallback()
        synchronizationInProgress = false
        synchronizationService =
            OfflineTelemetrySynchronizationService(
                collector = AndroidLocationCollector(this),
                technicalCollector = AndroidTechnicalTelemetryCollector(this),
                sender =
                    BatchLocationApiSender(
                        instanceUrl = instanceUrl,
                        deviceId = deviceId,
                        agentInstanceId = agentInstanceId,
                        keyStore = AgentKeyStore(),
                        apiClient = BatchLocationApiClient(),
                        diagnosticsStore = diagnosticsStore,
                    ),
                queue = FileTelemetryOfflineQueue(File(filesDir, TELEMETRY_QUEUE_FILE)),
                eventIdProvider = { UUID.randomUUID().toString() },
            )
        registerNetworkCallback()
        synchronize()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        unregisterNetworkCallback()
        synchronizationInProgress = false
        synchronizationService = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun synchronize() {
        val service = synchronizationService ?: return
        if (synchronizationInProgress) return
        synchronizationInProgress = true
        diagnosticsStore.recordSyncAttempt(Instant.now())
        service.synchronize { result ->
            handler.post {
                if (synchronizationService !== service) return@post
                synchronizationInProgress = false
                Log.i(TAG, "Resultado da sincronizacao de telemetria: $result")
                updateNotification(statusMessage(result))
                handler.postDelayed(::synchronize, LOCATION_INTERVAL_MS)
            }
        }
    }

    private fun retryPending() {
        val service = synchronizationService ?: return
        if (synchronizationInProgress) return
        synchronizationInProgress = true
        diagnosticsStore.recordSyncAttempt(Instant.now())
        service.retryPending { result ->
            handler.post {
                if (synchronizationService !== service) return@post
                synchronizationInProgress = false
                Log.i(TAG, "Resultado do retry da fila de telemetria: $result")
                updateNotification(statusMessage(result))
            }
        }
    }

    private fun registerNetworkCallback() {
        val manager = getSystemService(ConnectivityManager::class.java)
        val callback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    handler.post(::retryPending)
                }
            }
        manager.registerDefaultNetworkCallback(callback)
        networkCallback = callback
    }

    private fun unregisterNetworkCallback() {
        val callback = networkCallback ?: return
        runCatching {
            getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(callback)
        }
        networkCallback = null
    }

    private fun statusMessage(result: LocationSynchronizationResult): String {
        return when (result) {
            is LocationSynchronizationResult.Sent ->
                when {
                    result.pendingEvents > 0 ->
                        "Lote enviado; ${result.pendingEvents} eventos ainda aguardam sincronizacao."
                    result.locationStatus == LocationCollectionStatus.COLLECTED ->
                        "Telemetria e localizacao enviadas. Proxima coleta em 1 minuto."
                    result.locationStatus == null -> "Fila offline sincronizada."
                    else -> "Telemetria enviada sem localizacao. Proxima coleta em 1 minuto."
                }
            is LocationSynchronizationResult.SendFailure ->
                "Sem conexao: ${result.pendingEvents} eventos preservados localmente."
        }
    }

    private fun updateNotification(message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID,
            notification(message),
        )
    }

    private fun startAsForeground(message: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification(message))
            return
        }
        var serviceTypes = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            serviceTypes = serviceTypes or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        }
        startForeground(NOTIFICATION_ID, notification(message), serviceTypes)
    }

    private fun notification(message: String): Notification {
        val openApp =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("SimpleGuard Agent")
            .setContentText(message)
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sincronizacao de telemetria",
                NotificationManager.IMPORTANCE_LOW,
            )
        channel.description = "Estado da coleta e envio de telemetria do agente pareado."
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "SimpleGuardLocation"
        private const val EXTRA_INSTANCE_URL = "instanceUrl"
        private const val EXTRA_DEVICE_ID = "deviceId"
        private const val EXTRA_AGENT_INSTANCE_ID = "agentInstanceId"
        private const val NOTIFICATION_CHANNEL_ID = "simpleguard-location"
        private const val NOTIFICATION_ID = 3101
        private const val LOCATION_INTERVAL_MS = 60_000L
        private const val TELEMETRY_QUEUE_FILE = "telemetry-offline-queue.json"

        fun intent(
            context: Context,
            instanceUrl: String,
            deviceId: String,
            agentInstanceId: String,
        ): Intent {
            return Intent(context, LocationTrackingService::class.java)
                .putExtra(EXTRA_INSTANCE_URL, instanceUrl)
                .putExtra(EXTRA_DEVICE_ID, deviceId)
                .putExtra(EXTRA_AGENT_INSTANCE_ID, agentInstanceId)
        }
    }
}
