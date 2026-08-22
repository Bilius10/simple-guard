package simple.guard.agent

import simple.guard.agent.location.LocationDiagnosticsSnapshot
import simple.guard.agent.pairing.PairingUiState
import simple.guard.agent.unpairing.UnpairingUiState

internal class MainActivityScreenRenderer {
    private val pairingRenderer = PairingScreenRenderer()
    private val unpairingRenderer = UnpairingScreenRenderer()
    private val diagnosticsRenderer = DiagnosticsScreenRenderer()

    fun renderPairing(
        views: PairingScreenViews,
        state: PairingUiState,
    ) = pairingRenderer.render(views, state)

    fun renderUnpairing(
        views: UnpairingScreenViews,
        state: UnpairingUiState,
    ) = unpairingRenderer.render(views, state)

    fun renderDiagnostics(
        views: DiagnosticsScreenViews,
        snapshot: LocationDiagnosticsSnapshot,
        pairing: LocalPairing,
    ) = diagnosticsRenderer.render(views, snapshot, pairing)
}
