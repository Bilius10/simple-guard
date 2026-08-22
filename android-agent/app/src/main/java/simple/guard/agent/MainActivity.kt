package simple.guard.agent

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import simple.guard.agent.location.LocationDiagnosticsStore
import simple.guard.agent.location.LocationTrackingService
import simple.guard.agent.pairing.AgentKeyStore
import simple.guard.agent.pairing.CompletePairingRequest
import simple.guard.agent.pairing.PairingApiClient
import simple.guard.agent.pairing.PairingApiException
import simple.guard.agent.pairing.PairingStage
import simple.guard.agent.pairing.PairingUiController
import simple.guard.agent.unpairing.AgentPairingStatusResponse
import simple.guard.agent.unpairing.UnpairingApiClient
import simple.guard.agent.unpairing.UnpairingApiException
import simple.guard.agent.unpairing.UnpairingRequestContract
import simple.guard.agent.unpairing.UnpairingUiController
import simple.guard.agent.welcome.AgentScreen
import simple.guard.agent.welcome.WelcomeUiController
import java.io.IOException

class MainActivity : Activity() {
    private val uiController = PairingUiController()
    private val apiClient = PairingApiClient()
    private val keyStore = AgentKeyStore()
    private val unpairingUiController = UnpairingUiController()
    private val unpairingApiClient = UnpairingApiClient()
    private val welcomeUiController = WelcomeUiController()

    private lateinit var diagnosticsStore: LocationDiagnosticsStore
    private lateinit var preferencesStore: AgentPreferencesStore
    private lateinit var screenFactory: MainActivityScreenFactory
    private lateinit var screenRenderer: MainActivityScreenRenderer
    private lateinit var pairingViews: PairingScreenViews
    private lateinit var unpairingViews: UnpairingScreenViews
    private lateinit var diagnosticsViews: DiagnosticsScreenViews
    private var currentPairing: LocalPairing? = null

    @Volatile
    private var unpairingPolling = false

    @Volatile
    private var unpairingScreenActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diagnosticsStore = LocationDiagnosticsStore(this)
        preferencesStore = AgentPreferencesStore(this)
        screenFactory = MainActivityScreenFactory(this)
        screenRenderer = MainActivityScreenRenderer()
        showInitialScreen()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        val pairing = preferencesStore.loadLocalPairing() ?: return
        if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            startLocationTracking(pairing)
            return
        }

        diagnosticsStore.recordPermissionDenied("Permissao de localizacao negada pelo usuario.")
        launchTelemetryTracking(pairing)
        Toast.makeText(this, "Permissao de localizacao negada.", Toast.LENGTH_LONG).show()
    }

    private fun showInitialScreen() {
        when (welcomeUiController.initialScreen(preferencesStore.loadLocalPairing() != null)) {
            AgentScreen.WELCOME -> showWelcomeScreen()
            AgentScreen.PAIRED -> showUnpairingScreen()
            AgentScreen.PAIRING -> showPairingScreen()
        }
    }

    private fun showWelcomeScreen() {
        stopUnpairingPolling()
        unpairingScreenActive = false
        currentPairing = null
        val state = welcomeUiController.welcome()
        setContentView(
            screenFactory.buildWelcomeContent(state) {
                if (welcomeUiController.startPairing() == AgentScreen.PAIRING) {
                    showPairingScreen()
                }
            },
        )
    }

    private fun showPairingScreen() {
        stopUnpairingPolling()
        unpairingScreenActive = false
        currentPairing = null
        pairingViews = screenFactory.buildPairingContent()
        setContentView(pairingViews.root)
        screenRenderer.renderPairing(pairingViews, uiController.waiting())
        pairingViews.actionButton.setOnClickListener { submitPairing() }
    }

    private fun showPairedScreen(pairing: LocalPairing) {
        stopUnpairingPolling()
        unpairingScreenActive = false
        currentPairing = pairing
        pairingViews = screenFactory.buildPairingContent()
        setContentView(pairingViews.root)
        screenRenderer.renderPairing(pairingViews, uiController.paired(pairing.deviceName))
        pairingViews.actionButton.isEnabled = true
        pairingViews.actionButton.text = "Ver vinculo"
        pairingViews.actionButton.setOnClickListener { showUnpairingScreen() }
    }

    private fun submitPairing() {
        val instanceUrl = pairingViews.instanceUrlField.text.toString()
        val pairingCode = pairingViews.pairingCodeField.text.toString()
        val deviceName = pairingViews.deviceNameField.text.toString().ifBlank { DEFAULT_DEVICE_NAME }
        if (!renderPairingValidation(instanceUrl, pairingCode)) {
            return
        }

        pairingViews.actionButton.isEnabled = false
        Thread { completePairing(instanceUrl, pairingCode, deviceName) }.start()
    }

    private fun showUnpairingScreen() {
        stopUnpairingPolling()
        unpairingScreenActive = true
        val pairing = preferencesStore.loadLocalPairing()
        if (pairing == null) {
            showPairingScreen()
            return
        }

        currentPairing = pairing
        unpairingViews = screenFactory.buildUnpairingContent(pairing)
        setContentView(unpairingViews.root)
        if (pairing.pendingSynchronization) {
            screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.syncPending())
            unpairingViews.cancelButton.isEnabled = false
            unpairingViews.actionButton.setOnClickListener { requestUnpairing() }
        } else {
            screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.confirmationRequired())
            unpairingViews.cancelButton.setOnClickListener { cancelUnpairing() }
            unpairingViews.actionButton.setOnClickListener { confirmUnpairing() }
        }
        synchronizeUnpairingStatus(pairing)
        if (!pairing.pendingSynchronization) {
            startLocationTracking(pairing)
        }
        unpairingViews.diagnosticsButton.setOnClickListener { showDiagnosticsScreen(pairing) }
    }

    private fun showDiagnosticsScreen(pairing: LocalPairing) {
        stopUnpairingPolling()
        unpairingScreenActive = false
        currentPairing = pairing
        diagnosticsViews = screenFactory.buildDiagnosticsContent(pairing)
        setContentView(diagnosticsViews.root)
        screenRenderer.renderDiagnostics(diagnosticsViews, diagnosticsStore.snapshot(), pairing)
        diagnosticsViews.refreshButton.setOnClickListener {
            screenRenderer.renderDiagnostics(diagnosticsViews, diagnosticsStore.snapshot(), pairing)
        }
        diagnosticsViews.backButton.setOnClickListener { showUnpairingScreen() }
    }

    private fun startLocationTracking(pairing: LocalPairing) {
        if (hasLocationPermission()) {
            launchTelemetryTracking(pairing)
            return
        }

        Log.i(TAG, "Solicitando permissao de localizacao para iniciar o rastreamento.")
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            LOCATION_PERMISSION_REQUEST_CODE,
        )
    }

    private fun launchTelemetryTracking(pairing: LocalPairing) {
        Log.i(TAG, "Iniciando servico de telemetria para a instancia pareada.")
        try {
            startForegroundService(
                LocationTrackingService.intent(
                    context = this,
                    instanceUrl = pairing.instanceUrl,
                    deviceId = pairing.deviceId,
                    agentInstanceId = preferencesStore.agentInstanceId(),
                ),
            )
        } catch (exception: RuntimeException) {
            Log.e(TAG, "Nao foi possivel iniciar o servico de telemetria.", exception)
            diagnosticsStore.recordSendFailure("Nao foi possivel iniciar a telemetria em segundo plano.")
            Toast.makeText(
                this,
                "Agente aberto. Telemetria em segundo plano indisponivel neste momento.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun cancelUnpairing() {
        val pairing = currentPairing ?: preferencesStore.loadLocalPairing()
        if (pairing == null) {
            showPairingScreen()
        } else {
            showPairedScreen(pairing)
        }
    }

    private fun confirmUnpairing() {
        val pairing = currentPairing ?: return
        AlertDialog.Builder(this)
            .setTitle("Desparear ${pairing.deviceName}?")
            .setMessage("As credenciais serao revogadas. O dispositivo deixara de enviar telemetria e receber comandos.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Desparear") { _, _ -> requestUnpairing() }
            .show()
    }

    private fun requestUnpairing() {
        val pairing = currentPairing ?: return
        prepareUnpairingRequest()
        Thread { submitUnpairingRequest(pairing) }.start()
    }

    private fun startUnpairingPolling(pairing: LocalPairing) {
        unpairingPolling = true
        unpairingViews.statusBadge.postDelayed(
            {
                if (unpairingPolling && unpairingScreenActive) {
                    synchronizeUnpairingStatus(pairing)
                }
            },
            UNPAIRING_POLL_INTERVAL_MS,
        )
    }

    private fun stopUnpairingPolling() {
        unpairingPolling = false
    }

    private fun renderOfflinePairingStatus(pairing: LocalPairing) {
        if (!unpairingScreenActive || currentPairing?.deviceId != pairing.deviceId) {
            return
        }

        if (pairing.pendingSynchronization || unpairingPolling) {
            screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.syncPending())
            unpairingViews.cancelButton.isEnabled = false
            unpairingViews.actionButton.isEnabled = true
            unpairingViews.actionButton.setOnClickListener { requestUnpairing() }
            startUnpairingPolling(pairing)
            return
        }

        screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.confirmationRequired())
        unpairingViews.footerStatus.text = "Sem conexao; vinculo local carregado"
        unpairingViews.cancelButton.isEnabled = true
        unpairingViews.actionButton.isEnabled = true
        unpairingViews.actionButton.setOnClickListener { confirmUnpairing() }
    }

    private fun synchronizeUnpairingStatus(pairing: LocalPairing) {
        Thread { loadUnpairingStatus(pairing) }.start()
    }

    private fun renderPairingValidation(
        instanceUrl: String,
        pairingCode: String,
    ): Boolean {
        val validating = uiController.validating(instanceUrl, pairingCode)
        screenRenderer.renderPairing(pairingViews, validating)
        return validating.stage != PairingStage.FAILURE
    }

    private fun completePairing(
        instanceUrl: String,
        pairingCode: String,
        deviceName: String,
    ) {
        try {
            Log.i(TAG, "Iniciando pareamento com a instancia informada pelo usuario.")
            val pairing = performPairing(instanceUrl, pairingCode, deviceName)
            preferencesStore.persistPairing(pairing)
            currentPairing = pairing
            runOnUiThread { renderSuccessfulPairing(pairing) }
        } catch (exception: PairingApiException) {
            Log.w(TAG, "Pareamento recusado pela instancia: ${exception.userMessage}")
            runOnUiThread { renderPairingApiFailure(exception) }
        } catch (exception: IOException) {
            Log.e(TAG, "Falha de rede ao parear com a instancia.", exception)
            runOnUiThread { renderPairingConnectionFailure() }
        } catch (exception: RuntimeException) {
            Log.e(TAG, "Falha inesperada ao concluir o pareamento.", exception)
            runOnUiThread { renderPairingConnectionFailure() }
        }
    }

    private fun performPairing(
        instanceUrl: String,
        pairingCode: String,
        deviceName: String,
    ): LocalPairing {
        val agentInstanceId = preferencesStore.agentInstanceId()
        val response =
            apiClient.complete(
                instanceUrl,
                CompletePairingRequest(
                    pairingCode = pairingCode,
                    agentInstanceId = agentInstanceId,
                    platform = "ANDROID",
                    publicKey = keyStore.publicKey(agentInstanceId),
                ),
            )
        return LocalPairing(
            deviceId = response.deviceId,
            deviceName = response.deviceName.ifBlank { deviceName },
            instanceUrl = instanceUrl,
            pendingSynchronization = false,
        )
    }

    private fun renderSuccessfulPairing(pairing: LocalPairing) {
        screenRenderer.renderPairing(pairingViews, uiController.paired(pairing.deviceName))
        pairingViews.actionButton.isEnabled = true
        pairingViews.actionButton.text = "Ver vinculo"
        pairingViews.actionButton.setOnClickListener { showUnpairingScreen() }
        startLocationTracking(pairing)
    }

    private fun renderPairingApiFailure(exception: PairingApiException) {
        screenRenderer.renderPairing(
            pairingViews,
            if (exception.expired) {
                uiController.expired()
            } else {
                uiController.failed(exception.userMessage)
            },
        )
        pairingViews.actionButton.isEnabled = true
    }

    private fun renderPairingConnectionFailure() {
        screenRenderer.renderPairing(
            pairingViews,
            uiController.failed("Nao foi possivel conectar com a instancia."),
        )
        pairingViews.actionButton.isEnabled = true
    }

    private fun prepareUnpairingRequest() {
        screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.requested())
        unpairingViews.footerStatus.text = "Enviando solicitacao"
        unpairingViews.actionButton.text = "Enviando..."
        unpairingViews.cancelButton.isEnabled = false
        unpairingViews.actionButton.isEnabled = false
    }

    private fun submitUnpairingRequest(pairing: LocalPairing) {
        try {
            val agentInstanceId = preferencesStore.agentInstanceId()
            val response = requestUnpairingFromApi(pairing, agentInstanceId)
            UnpairingRequestContract.requirePendingRequest(response)
            runOnUiThread { renderRequestedUnpairing(pairing) }
        } catch (exception: UnpairingApiException) {
            runOnUiThread { renderUnpairingFailure(exception.userMessage) }
        } catch (exception: IOException) {
            storePendingUnpairing(pairing, exception)
            runOnUiThread { renderPendingUnpairingSynchronization() }
        } catch (exception: RuntimeException) {
            runOnUiThread {
                renderUnpairingFailure(exception.message ?: "Nao foi possivel usar a credencial local.")
            }
        }
    }

    private fun requestUnpairingFromApi(
        pairing: LocalPairing,
        agentInstanceId: String,
    ) = unpairingApiClient.unpair(
        instanceUrl = pairing.instanceUrl,
        deviceId = pairing.deviceId,
        agentInstanceId = agentInstanceId,
        signature = keyStore.signUnpairing(agentInstanceId, pairing.deviceId),
    )

    private fun renderRequestedUnpairing(pairing: LocalPairing) {
        screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.requested())
        unpairingViews.cancelButton.isEnabled = false
        unpairingViews.actionButton.isEnabled = false
        startUnpairingPolling(pairing)
    }

    private fun renderUnpairingFailure(message: String) {
        screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.apiFailure(message))
        unpairingViews.cancelButton.isEnabled = true
        unpairingViews.actionButton.isEnabled = true
    }

    private fun storePendingUnpairing(
        pairing: LocalPairing,
        exception: IOException,
    ) {
        stopService(Intent(this, LocationTrackingService::class.java))
        preferencesStore.persistPendingUnpairing(pairing)
        currentPairing = pairing.copy(pendingSynchronization = true)
        Log.w(TAG, "Falha de rede ao enviar solicitacao de despareamento. Mantendo pedido pendente.", exception)
    }

    private fun renderPendingUnpairingSynchronization() {
        screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.syncPending())
        unpairingViews.cancelButton.isEnabled = false
        unpairingViews.actionButton.isEnabled = true
        unpairingViews.actionButton.setOnClickListener { requestUnpairing() }
    }

    private fun loadUnpairingStatus(pairing: LocalPairing) {
        try {
            val agentInstanceId = preferencesStore.agentInstanceId()
            val response = requestUnpairingStatus(pairing, agentInstanceId)
            runOnUiThread { renderLoadedUnpairingStatus(pairing, response, agentInstanceId) }
        } catch (exception: IOException) {
            Log.i(TAG, "Sem conexao para consultar status do vinculo local.", exception)
            runOnUiThread { renderOfflinePairingStatus(pairing) }
        } catch (exception: RuntimeException) {
            runOnUiThread { renderUnpairingStatusFailure(pairing, exception) }
        }
    }

    private fun requestUnpairingStatus(
        pairing: LocalPairing,
        agentInstanceId: String,
    ) = unpairingApiClient.pairingStatus(
        instanceUrl = pairing.instanceUrl,
        deviceId = pairing.deviceId,
        agentInstanceId = agentInstanceId,
        signature = keyStore.signUnpairing(agentInstanceId, pairing.deviceId),
    )

    private fun renderLoadedUnpairingStatus(
        pairing: LocalPairing,
        response: AgentPairingStatusResponse,
        agentInstanceId: String,
    ) {
        if (!isCurrentUnpairingScreen(pairing)) {
            return
        }

        when {
            response.pairingStatus == "unpaired" -> handleUnpairedStatus(agentInstanceId)
            response.unpairingStatus == "pending" -> handlePendingUnpairingStatus(pairing)
            response.unpairingStatus == "rejected" -> handleRejectedUnpairingStatus()
            else -> stopUnpairingPolling()
        }
    }

    private fun isCurrentUnpairingScreen(pairing: LocalPairing): Boolean =
        unpairingScreenActive && currentPairing?.deviceId == pairing.deviceId

    private fun handleUnpairedStatus(agentInstanceId: String) {
        stopUnpairingPolling()
        stopService(Intent(this, LocationTrackingService::class.java))
        runCatching { keyStore.delete(agentInstanceId) }
        preferencesStore.clearLocalPairing()
        currentPairing = null
        screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.unpaired())
        unpairingViews.cancelButton.isEnabled = false
        unpairingViews.actionButton.isEnabled = true
        unpairingViews.actionButton.setOnClickListener { showWelcomeScreen() }
    }

    private fun handlePendingUnpairingStatus(pairing: LocalPairing) {
        screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.requested())
        unpairingViews.cancelButton.isEnabled = false
        unpairingViews.actionButton.isEnabled = false
        startUnpairingPolling(pairing)
    }

    private fun handleRejectedUnpairingStatus() {
        stopUnpairingPolling()
        screenRenderer.renderUnpairing(unpairingViews, unpairingUiController.rejected())
        unpairingViews.cancelButton.isEnabled = true
        unpairingViews.actionButton.isEnabled = true
        unpairingViews.actionButton.setOnClickListener { confirmUnpairing() }
    }

    private fun renderUnpairingStatusFailure(
        pairing: LocalPairing,
        exception: RuntimeException,
    ) {
        if (!isCurrentUnpairingScreen(pairing)) {
            return
        }

        screenRenderer.renderUnpairing(
            unpairingViews,
            unpairingUiController.apiFailure(
                exception.message ?: "Nao foi possivel consultar o despareamento.",
            ),
        )
        if (unpairingPolling) {
            startUnpairingPolling(pairing)
        }
    }

    private companion object {
        const val TAG = "SimpleGuardAgent"
        const val DEFAULT_DEVICE_NAME = "Dispositivo Android"
        const val LOCATION_PERMISSION_REQUEST_CODE = 3001
        const val UNPAIRING_POLL_INTERVAL_MS = 5000L
    }
}
