package simple.guard.agent

import android.widget.TextView
import simple.guard.agent.location.LocationDiagnosticStatus
import simple.guard.agent.location.LocationDiagnosticsSnapshot
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal class DiagnosticsScreenRenderer {
    fun render(
        views: DiagnosticsScreenViews,
        snapshot: LocationDiagnosticsSnapshot,
        pairing: LocalPairing,
    ) {
        renderIdentity(views, pairing)
        renderSynchronization(views, snapshot)
        renderTechnicalState(views, snapshot)
        renderFooter(views, snapshot.status)
    }

    private fun renderIdentity(
        views: DiagnosticsScreenViews,
        pairing: LocalPairing,
    ) {
        views.deviceValue.text = pairing.deviceName
        views.instanceValue.text = pairing.instanceUrl
    }

    private fun renderSynchronization(
        views: DiagnosticsScreenViews,
        snapshot: LocationDiagnosticsSnapshot,
    ) {
        views.attemptValue.text = formatInstant(snapshot.lastAttemptAt)
        views.successValue.text = formatInstant(snapshot.lastSuccessAt)
        views.statusValue.text = snapshot.status.label
        views.statusValue.setTextColor(diagnosticStatusColor(snapshot.status))
        renderValue(views.locationStatusValue, snapshot.locationStatus?.name)
        renderValue(views.providerValue, snapshot.provider)
        renderValue(
            views.failureValue,
            snapshot.failureReason,
            diagnosticStatusColor(snapshot.status),
        )
    }

    private fun renderTechnicalState(
        views: DiagnosticsScreenViews,
        snapshot: LocationDiagnosticsSnapshot,
    ) {
        renderBattery(views, snapshot)
        renderCharging(views, snapshot)
        renderValue(views.networkValue, snapshot.networkType?.name)
        renderValue(views.signalValue, snapshot.signalStrengthDbm?.let { "$it dBm" })
        renderValue(views.finePermissionValue, snapshot.fineLocationPermission?.name)
        renderValue(views.coarsePermissionValue, snapshot.coarseLocationPermission?.name)
    }

    private fun renderBattery(
        views: DiagnosticsScreenViews,
        snapshot: LocationDiagnosticsSnapshot,
    ) {
        views.batteryValue.text = snapshot.batteryLevelPercentage?.let { "$it%" } ?: "-"
        views.batteryValue.setTextColor(
            when {
                snapshot.batteryLevelPercentage == null -> AgentScreenTheme.MUTED
                snapshot.batteryLevelPercentage <= 15 -> AgentScreenTheme.WARNING
                else -> AgentScreenTheme.TEXT
            },
        )
    }

    private fun renderCharging(
        views: DiagnosticsScreenViews,
        snapshot: LocationDiagnosticsSnapshot,
    ) {
        val value =
            snapshot.batteryCharging?.let { charging ->
                if (charging) {
                    "Carregando"
                } else {
                    "Descarregando"
                }
            }
        renderValue(views.chargingValue, value)
    }

    private fun renderFooter(
        views: DiagnosticsScreenViews,
        status: LocationDiagnosticStatus,
    ) {
        views.footerStatus.text =
            when (status) {
                LocationDiagnosticStatus.SENT -> "Ultimo envio aceito pela API"
                LocationDiagnosticStatus.SEND_FAILURE -> "Ultimo envio de telemetria falhou"
                LocationDiagnosticStatus.LOCATION_UNAVAILABLE -> "Agente nao conseguiu obter um ponto valido"
                LocationDiagnosticStatus.PROVIDER_UNAVAILABLE -> "Nenhum provedor de localizacao disponivel"
                LocationDiagnosticStatus.PERMISSION_DENIED -> "Permissao de localizacao ausente no dispositivo"
                LocationDiagnosticStatus.IDLE -> "Aguardando a primeira sincronizacao do servico"
            }
    }

    private fun renderValue(
        view: TextView,
        value: String?,
        activeColor: Int = AgentScreenTheme.TEXT,
    ) {
        view.text = value ?: "-"
        view.setTextColor(
            if (value.isNullOrBlank()) {
                AgentScreenTheme.MUTED
            } else {
                activeColor
            },
        )
    }

    private fun formatInstant(value: Instant?): String =
        value?.atZone(ZoneId.systemDefault())?.format(DIAGNOSTIC_DATE_TIME_FORMATTER) ?: "-"

    private fun diagnosticStatusColor(status: LocationDiagnosticStatus): Int =
        when (status) {
            LocationDiagnosticStatus.SENT -> AgentScreenTheme.SUCCESS
            LocationDiagnosticStatus.SEND_FAILURE,
            LocationDiagnosticStatus.LOCATION_UNAVAILABLE,
            LocationDiagnosticStatus.PROVIDER_UNAVAILABLE,
            LocationDiagnosticStatus.PERMISSION_DENIED,
            -> AgentScreenTheme.DANGER
            LocationDiagnosticStatus.IDLE -> AgentScreenTheme.MUTED
        }

    private companion object {
        val DIAGNOSTIC_DATE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM HH:mm:ss")
    }
}
