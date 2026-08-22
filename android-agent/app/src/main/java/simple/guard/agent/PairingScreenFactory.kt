package simple.guard.agent

import android.app.Activity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView

internal class PairingScreenFactory(
    activity: Activity,
) : BaseScreenFactory(activity) {
    fun build(): PairingScreenViews {
        val root = screenRoot()
        val content = contentRoot()
        root.addView(header())
        root.addView(content.view, content.layoutParams)

        val statusSection = buildStatusSection(content.view)
        val codeSection = buildCodeSection(content.view)
        content.view.addView(spacer())
        val actionButton = commandButton("Validar codigo")
        content.view.addView(actionButton)
        val footerStatus = footer("Pareamento ainda nao concluido")
        root.addView(footerStatus)

        return PairingScreenViews(
            root = scrollView(root),
            instanceUrlField = statusSection.instanceUrlField,
            pairingCodeField = codeSection.pairingCodeField,
            deviceNameField = codeSection.deviceNameField,
            statusBadge = statusSection.statusBadge,
            apiStatusValue = statusSection.apiStatusValue,
            qrStatusValue = statusSection.qrStatusValue,
            statePrimaryValue = codeSection.statePrimaryValue,
            stateDetailValue = codeSection.stateDetailValue,
            stateSuccessValue = codeSection.stateSuccessValue,
            stateFailureValue = codeSection.stateFailureValue,
            footerStatus = footerStatus,
            actionButton = actionButton,
        )
    }

    private fun buildStatusSection(contentView: android.widget.LinearLayout): PairingStatusSection {
        val statusBadge = badge("")
        val instanceUrlField = editableRow("URL instancia", "http://192.168.1.5:8080")
        val apiStatusValue = valueRow("API", "Conectando", AgentScreenTheme.WARNING)
        val qrStatusValue = valueRow("QR code", "Leitor ativo", AgentScreenTheme.TEXT)
        contentView.addView(
            buildPairingPanel(statusBadge, instanceUrlField, apiStatusValue, qrStatusValue),
            bottomMargin(dp(18)),
        )
        return PairingStatusSection(statusBadge, instanceUrlField, apiStatusValue, qrStatusValue)
    }

    private fun buildCodeSection(contentView: android.widget.LinearLayout): PairingCodeSection {
        val deviceNameField =
            editableRow("Nome sugerido", "Android Entrega 03").apply {
                imeOptions = EditorInfo.IME_ACTION_DONE
            }
        val pairingCodeField =
            editableRow("Codigo manual", "PXYY-4XFA").apply {
                imeOptions = EditorInfo.IME_ACTION_NEXT
            }
        val statePrimaryValue = valueRow("Estado 1", "Aguardando codigo", AgentScreenTheme.TEXT)
        val stateDetailValue = valueRow("Estado 2", "Codigo expirado", AgentScreenTheme.DANGER)
        val stateSuccessValue = valueRow("Estado 3", "Pareado com sucesso", AgentScreenTheme.SUCCESS)
        val stateFailureValue = valueRow("Estado 4", "Falha de pareamento", AgentScreenTheme.DANGER)
        contentView.addView(
            buildCodePanel(
                deviceNameField,
                pairingCodeField,
                statePrimaryValue,
                stateDetailValue,
                stateSuccessValue,
                stateFailureValue,
            ),
            bottomMargin(dp(18)),
        )
        return PairingCodeSection(
            deviceNameField,
            pairingCodeField,
            statePrimaryValue,
            stateDetailValue,
            stateSuccessValue,
            stateFailureValue,
        )
    }

    private fun buildPairingPanel(
        statusBadge: TextView,
        instanceUrlField: EditText,
        apiStatusValue: TextView,
        qrStatusValue: TextView,
    ) = panel("Pareamento").apply {
        addView(statusBadge, wrapBottomMargin(dp(10)))
        addView(instanceUrlField.parent as View, bottomMargin(dp(7)))
        addView(apiStatusValue.parent as View, bottomMargin(dp(7)))
        addView(qrStatusValue.parent as View)
    }

    private fun buildCodePanel(
        deviceNameField: EditText,
        pairingCodeField: EditText,
        statePrimaryValue: TextView,
        stateDetailValue: TextView,
        stateSuccessValue: TextView,
        stateFailureValue: TextView,
    ) = panel("Codigo / QR").apply {
        addView(deviceNameField.parent as View, bottomMargin(dp(7)))
        addView(pairingCodeField.parent as View, bottomMargin(dp(7)))
        addView(statePrimaryValue.parent as View, bottomMargin(dp(7)))
        addView(stateDetailValue.parent as View, bottomMargin(dp(7)))
        addView(stateSuccessValue.parent as View, bottomMargin(dp(7)))
        addView(stateFailureValue.parent as View)
    }

    private data class PairingStatusSection(
        val statusBadge: TextView,
        val instanceUrlField: EditText,
        val apiStatusValue: TextView,
        val qrStatusValue: TextView,
    )

    private data class PairingCodeSection(
        val deviceNameField: EditText,
        val pairingCodeField: EditText,
        val statePrimaryValue: TextView,
        val stateDetailValue: TextView,
        val stateSuccessValue: TextView,
        val stateFailureValue: TextView,
    )
}
