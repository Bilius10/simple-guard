package simple.guard.agent

import android.app.Activity
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import simple.guard.agent.welcome.WelcomeUiState

internal data class PairingScreenViews(
    val root: ScrollView,
    val instanceUrlField: EditText,
    val pairingCodeField: EditText,
    val deviceNameField: EditText,
    val statusBadge: TextView,
    val apiStatusValue: TextView,
    val qrStatusValue: TextView,
    val statePrimaryValue: TextView,
    val stateDetailValue: TextView,
    val stateSuccessValue: TextView,
    val stateFailureValue: TextView,
    val footerStatus: TextView,
    val actionButton: Button,
)

internal data class UnpairingScreenViews(
    val root: ScrollView,
    val statusBadge: TextView,
    val requestedValue: TextView,
    val unpairedValue: TextView,
    val failureValue: TextView,
    val pendingValue: TextView,
    val detailValue: TextView,
    val footerStatus: TextView,
    val cancelButton: Button,
    val actionButton: Button,
    val diagnosticsButton: Button,
)

internal data class DiagnosticsScreenViews(
    val root: ScrollView,
    val attemptValue: TextView,
    val successValue: TextView,
    val statusValue: TextView,
    val locationStatusValue: TextView,
    val providerValue: TextView,
    val failureValue: TextView,
    val batteryValue: TextView,
    val chargingValue: TextView,
    val networkValue: TextView,
    val signalValue: TextView,
    val finePermissionValue: TextView,
    val coarsePermissionValue: TextView,
    val instanceValue: TextView,
    val deviceValue: TextView,
    val refreshButton: Button,
    val backButton: Button,
    val footerStatus: TextView,
)

internal class MainActivityScreenFactory(
    activity: Activity,
) {
    private val welcomeFactory = WelcomeScreenFactory(activity)
    private val pairingFactory = PairingScreenFactory(activity)
    private val unpairingFactory = UnpairingScreenFactory(activity)
    private val diagnosticsFactory = DiagnosticsScreenFactory(activity)

    fun buildWelcomeContent(
        state: WelcomeUiState,
        onStartPairing: () -> Unit,
    ): ScrollView = welcomeFactory.build(state, onStartPairing)

    fun buildPairingContent(): PairingScreenViews = pairingFactory.build()

    fun buildUnpairingContent(pairing: LocalPairing): UnpairingScreenViews = unpairingFactory.build(pairing)

    fun buildDiagnosticsContent(pairing: LocalPairing): DiagnosticsScreenViews = diagnosticsFactory.build(pairing)
}
