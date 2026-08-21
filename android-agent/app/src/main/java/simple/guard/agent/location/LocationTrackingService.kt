package simple.guard.agent.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.Manifest
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.time.Instant
import java.util.UUID
import simple.guard.agent.MainActivity
import simple.guard.agent.pairing.AgentKeyStore

class LocationTrackingService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var synchronizationService: LocationSynchronizationService? = null
    private lateinit var diagnosticsStore: LocationDiagnosticsStore

    override fun onCreate() {
        super.onCreate()
        diagnosticsStore = LocationDiagnosticsStore(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        synchronizationService = LocationSynchronizationService(
            collector = AndroidLocationCollector(this),
            technicalCollector = AndroidTechnicalTelemetryCollector(this),
            sender = LocationApiSender(
                instanceUrl = instanceUrl,
                deviceId = deviceId,
                agentInstanceId = agentInstanceId,
                keyStore = AgentKeyStore(),
                apiClient = LocationApiClient(),
                diagnosticsStore = diagnosticsStore
            ),
            eventIdProvider = { UUID.randomUUID().toString() }
        )
        handler.removeCallbacksAndMessages(null)
        synchronize()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        synchronizationService = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun synchronize() {
        val service = synchronizationService ?: return
        diagnosticsStore.recordSyncAttempt(Instant.now())
        service.synchronize result@ { result ->
            if (synchronizationService !== service) {
                return@result
            }
            Log.i(TAG, "Resultado da sincronizacao de telemetria: $result")
            updateNotification(statusMessage(result))
            handler.postDelayed(::synchronize, LOCATION_INTERVAL_MS)
        }
    }

    private fun statusMessage(result: LocationSynchronizationResult): String {
        return when (result) {
            is LocationSynchronizationResult.Sent -> when (result.locationStatus) {
                LocationCollectionStatus.COLLECTED -> "Telemetria e localizacao enviadas. Proxima coleta em 1 minuto."
                else -> "Telemetria enviada sem localizacao. Proxima coleta em 1 minuto."
            }
            is LocationSynchronizationResult.SendFailure ->
                "Falha de rede ao enviar telemetria. Nova tentativa em 1 minuto."
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
            notification(message)
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
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Sincronizacao de telemetria",
            NotificationManager.IMPORTANCE_LOW
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

        fun intent(
            context: Context,
            instanceUrl: String,
            deviceId: String,
            agentInstanceId: String
        ): Intent {
            return Intent(context, LocationTrackingService::class.java)
                .putExtra(EXTRA_INSTANCE_URL, instanceUrl)
                .putExtra(EXTRA_DEVICE_ID, deviceId)
                .putExtra(EXTRA_AGENT_INSTANCE_ID, agentInstanceId)
        }
    }
}
