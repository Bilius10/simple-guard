package simple.guard.agent

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

internal class DiagnosticsScreenFactory(
    activity: Activity,
) : BaseScreenFactory(activity) {
    fun build(pairing: LocalPairing): DiagnosticsScreenViews {
        val root = screenRoot()
        val content = contentRoot()
        root.addView(header())
        root.addView(content.view, content.layoutParams)

        val summarySection = buildSummarySection(content.view, pairing)
        val synchronizationSection = buildSynchronizationSection(content.view)
        val technicalSection = buildTechnicalSection(content.view)
        content.view.addView(spacer())
        val actions = buildActions(content.view)
        val footerStatus = footer("Ultimo estado persistido localmente")
        root.addView(footerStatus)

        return DiagnosticsScreenViews(
            root = scrollView(root),
            attemptValue = synchronizationSection.attemptValue,
            successValue = synchronizationSection.successValue,
            statusValue = synchronizationSection.statusValue,
            locationStatusValue = synchronizationSection.locationStatusValue,
            providerValue = synchronizationSection.providerValue,
            failureValue = synchronizationSection.failureValue,
            batteryValue = technicalSection.batteryValue,
            chargingValue = technicalSection.chargingValue,
            networkValue = technicalSection.networkValue,
            signalValue = technicalSection.signalValue,
            finePermissionValue = technicalSection.finePermissionValue,
            coarsePermissionValue = technicalSection.coarsePermissionValue,
            instanceValue = summarySection.instanceValue,
            deviceValue = summarySection.deviceValue,
            refreshButton = actions.refreshButton,
            backButton = actions.backButton,
            footerStatus = footerStatus,
        )
    }

    private fun buildSummarySection(
        contentView: LinearLayout,
        pairing: LocalPairing,
    ): DiagnosticsSummarySection {
        val deviceValue = valueRow("Dispositivo", pairing.deviceName, AgentScreenTheme.TEXT)
        val instanceValue = valueRow("Instancia", pairing.instanceUrl, AgentScreenTheme.TEXT)
        contentView.addView(buildSummaryPanel(deviceValue, instanceValue), bottomMargin(dp(18)))
        return DiagnosticsSummarySection(deviceValue, instanceValue)
    }

    private fun buildSynchronizationSection(contentView: LinearLayout): DiagnosticsSyncSection {
        val attemptValue = valueRow("Ultima tentativa", "-", AgentScreenTheme.MUTED)
        val successValue = valueRow("Ultimo sucesso", "-", AgentScreenTheme.MUTED)
        val statusValue = valueRow("Status atual", "Aguardando coleta", AgentScreenTheme.TEXT)
        val locationStatusValue = valueRow("Localizacao", "-", AgentScreenTheme.MUTED)
        val providerValue = valueRow("Ultimo provedor", "-", AgentScreenTheme.MUTED)
        val failureValue = valueRow("Motivo da falha", "-", AgentScreenTheme.MUTED)
        contentView.addView(
            buildSynchronizationPanel(
                attemptValue,
                successValue,
                statusValue,
                locationStatusValue,
                providerValue,
                failureValue,
            ),
            bottomMargin(dp(18)),
        )
        return DiagnosticsSyncSection(
            attemptValue,
            successValue,
            statusValue,
            locationStatusValue,
            providerValue,
            failureValue,
        )
    }

    private fun buildTechnicalSection(contentView: LinearLayout): DiagnosticsTechnicalSection {
        val batteryValue = valueRow("Bateria", "-", AgentScreenTheme.MUTED)
        val chargingValue = valueRow("Carregamento", "-", AgentScreenTheme.MUTED)
        val networkValue = valueRow("Rede", "-", AgentScreenTheme.MUTED)
        val signalValue = valueRow("Sinal", "-", AgentScreenTheme.MUTED)
        val finePermissionValue = valueRow("Localizacao precisa", "-", AgentScreenTheme.MUTED)
        val coarsePermissionValue = valueRow("Localizacao aproximada", "-", AgentScreenTheme.MUTED)
        contentView.addView(
            buildTechnicalPanel(
                batteryValue,
                chargingValue,
                networkValue,
                signalValue,
                finePermissionValue,
                coarsePermissionValue,
            ),
            bottomMargin(dp(18)),
        )
        return DiagnosticsTechnicalSection(
            batteryValue,
            chargingValue,
            networkValue,
            signalValue,
            finePermissionValue,
            coarsePermissionValue,
        )
    }

    private fun buildActions(contentView: LinearLayout): DiagnosticsActions {
        val refreshButton = commandButton("Atualizar telemetria")
        val backButton =
            commandButton("Voltar").apply {
                background = AgentScreenTheme.bordered(0xFF202B3F.toInt(), 0xFF647287.toInt(), dp(1), dp(2))
            }
        contentView.addView(refreshButton, bottomMargin(dp(8)))
        contentView.addView(backButton)
        return DiagnosticsActions(refreshButton, backButton)
    }

    private fun buildSummaryPanel(
        deviceValue: TextView,
        instanceValue: TextView,
    ) = panel("Telemetria do agente").apply {
        addView(deviceValue.parent as View, bottomMargin(dp(7)))
        addView(instanceValue.parent as View)
    }

    private fun buildSynchronizationPanel(
        attemptValue: TextView,
        successValue: TextView,
        statusValue: TextView,
        locationStatusValue: TextView,
        providerValue: TextView,
        failureValue: TextView,
    ) = panel("Sincronizacao").apply {
        addView(attemptValue.parent as View, bottomMargin(dp(7)))
        addView(successValue.parent as View, bottomMargin(dp(7)))
        addView(statusValue.parent as View, bottomMargin(dp(7)))
        addView(locationStatusValue.parent as View, bottomMargin(dp(7)))
        addView(providerValue.parent as View, bottomMargin(dp(7)))
        addView(failureValue.parent as View)
    }

    private fun buildTechnicalPanel(
        batteryValue: TextView,
        chargingValue: TextView,
        networkValue: TextView,
        signalValue: TextView,
        finePermissionValue: TextView,
        coarsePermissionValue: TextView,
    ) = panel("Estado tecnico").apply {
        addView(batteryValue.parent as View, bottomMargin(dp(7)))
        addView(chargingValue.parent as View, bottomMargin(dp(7)))
        addView(networkValue.parent as View, bottomMargin(dp(7)))
        addView(signalValue.parent as View, bottomMargin(dp(7)))
        addView(finePermissionValue.parent as View, bottomMargin(dp(7)))
        addView(coarsePermissionValue.parent as View)
    }

    private data class DiagnosticsSummarySection(
        val deviceValue: TextView,
        val instanceValue: TextView,
    )

    private data class DiagnosticsSyncSection(
        val attemptValue: TextView,
        val successValue: TextView,
        val statusValue: TextView,
        val locationStatusValue: TextView,
        val providerValue: TextView,
        val failureValue: TextView,
    )

    private data class DiagnosticsTechnicalSection(
        val batteryValue: TextView,
        val chargingValue: TextView,
        val networkValue: TextView,
        val signalValue: TextView,
        val finePermissionValue: TextView,
        val coarsePermissionValue: TextView,
    )

    private data class DiagnosticsActions(
        val refreshButton: android.widget.Button,
        val backButton: android.widget.Button,
    )
}
