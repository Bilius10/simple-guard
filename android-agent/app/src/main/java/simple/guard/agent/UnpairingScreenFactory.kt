package simple.guard.agent

import android.app.Activity
import android.view.View

internal class UnpairingScreenFactory(
    activity: Activity,
) : BaseScreenFactory(activity) {
    fun build(pairing: LocalPairing): UnpairingScreenViews {
        val root = screenRoot()
        val content = contentRoot()
        root.addView(header())
        root.addView(content.view, content.layoutParams)

        val statusBadge = badge("")
        val detailValue = valueRow("Consequencia", "Para telemetria e comandos", AgentScreenTheme.TEXT)
        content.view.addView(
            buildSummaryPanel(pairing, statusBadge, detailValue),
            bottomMargin(dp(18)),
        )

        val requestedValue = valueRow("Estado 1", "Solicitado", AgentScreenTheme.MUTED)
        val unpairedValue = valueRow("Estado 2", "Despareado", AgentScreenTheme.MUTED)
        val failureValue = valueRow("Estado 3", "Falha API", AgentScreenTheme.MUTED)
        val pendingValue = valueRow("Estado 4", "Sync pendente", AgentScreenTheme.MUTED)
        content.view.addView(
            buildStatePanel(requestedValue, unpairedValue, failureValue, pendingValue),
            bottomMargin(dp(18)),
        )

        content.view.addView(spacer())
        val cancelButton = createCancelButton()
        val actionButton = createActionButton()
        val diagnosticsButton = createDiagnosticsButton()
        content.view.addView(cancelButton, bottomMargin(dp(8)))
        content.view.addView(actionButton, bottomMargin(dp(8)))
        content.view.addView(diagnosticsButton, bottomMargin(dp(8)))
        val footerStatus = footer("Nenhuma alteracao aplicada")
        root.addView(footerStatus)

        return UnpairingScreenViews(
            root = scrollView(root),
            statusBadge = statusBadge,
            requestedValue = requestedValue,
            unpairedValue = unpairedValue,
            failureValue = failureValue,
            pendingValue = pendingValue,
            detailValue = detailValue,
            footerStatus = footerStatus,
            cancelButton = cancelButton,
            actionButton = actionButton,
            diagnosticsButton = diagnosticsButton,
        )
    }

    private fun buildSummaryPanel(
        pairing: LocalPairing,
        statusBadge: android.widget.TextView,
        detailValue: android.widget.TextView,
    ) = panel("Despareamento").apply {
        addView(statusBadge, wrapBottomMargin(dp(10)))
        addView(valueRow("Dispositivo", pairing.deviceName, AgentScreenTheme.TEXT).parent as View, bottomMargin(dp(7)))
        addView(valueRow("Instancia atual", pairing.instanceUrl, AgentScreenTheme.TEXT).parent as View, bottomMargin(dp(7)))
        addView(detailValue.parent as View)
    }

    private fun buildStatePanel(
        requestedValue: android.widget.TextView,
        unpairedValue: android.widget.TextView,
        failureValue: android.widget.TextView,
        pendingValue: android.widget.TextView,
    ) = panel("Remover vinculo").apply {
        addView(requestedValue.parent as View, bottomMargin(dp(7)))
        addView(unpairedValue.parent as View, bottomMargin(dp(7)))
        addView(failureValue.parent as View, bottomMargin(dp(7)))
        addView(pendingValue.parent as View)
    }

    private fun createCancelButton() =
        commandButton("Cancelar").apply {
            background = AgentScreenTheme.bordered(0xFF202B3F.toInt(), 0xFF647287.toInt(), dp(1), dp(2))
        }

    private fun createActionButton() =
        commandButton("Desparear dispositivo").apply {
            background = AgentScreenTheme.bordered(0xFF471B28.toInt(), AgentScreenTheme.DANGER, dp(1), dp(2))
            setTextColor(0xFFFFD7D7.toInt())
        }

    private fun createDiagnosticsButton() =
        commandButton("Abrir telemetria").apply {
            background = AgentScreenTheme.bordered(0xFF1C3248.toInt(), 0xFF7CC7F7.toInt(), dp(1), dp(2))
        }
}
