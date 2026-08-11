package simple.guard.agent

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import simple.guard.agent.pairing.AgentKeyStore
import simple.guard.agent.pairing.CompletePairingRequest
import simple.guard.agent.pairing.PairingApiClient
import simple.guard.agent.pairing.PairingApiException
import simple.guard.agent.pairing.PairingStage
import simple.guard.agent.pairing.PairingUiController
import simple.guard.agent.pairing.PairingUiState

class MainActivity : Activity() {

    private val uiController = PairingUiController()
    private val apiClient = PairingApiClient()
    private val keyStore = AgentKeyStore()

    private lateinit var instanceUrlField: EditText
    private lateinit var pairingCodeField: EditText
    private lateinit var deviceNameField: EditText
    private lateinit var statusBadge: TextView
    private lateinit var apiStatusValue: TextView
    private lateinit var qrStatusValue: TextView
    private lateinit var statePrimaryValue: TextView
    private lateinit var stateDetailValue: TextView
    private lateinit var stateSuccessValue: TextView
    private lateinit var stateFailureValue: TextView
    private lateinit var footerStatus: TextView
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
        render(uiController.waiting())

        actionButton.setOnClickListener {
            submitPairing()
        }
    }

    private fun buildContent(): ScrollView {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            minimumHeight = resources.displayMetrics.heightPixels
            setBackgroundColor(SCREEN_BACKGROUND)
        }

        root.addView(header())

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(10))
        }
        root.addView(
            content,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val pairingPanel = panel("Pareamento")
        statusBadge = badge("")
        pairingPanel.addView(statusBadge, wrapBottomMargin(dp(10)))

        instanceUrlField = editableRow(
            label = "URL instancia",
            hint = "http://192.168.1.5:8080"
        )
        pairingPanel.addView(instanceUrlField.parent as View, bottomMargin(dp(7)))

        apiStatusValue = valueRow(
            label = "API",
            value = "Conectando",
            valueColor = WARNING
        )
        pairingPanel.addView(apiStatusValue.parent as View, bottomMargin(dp(7)))

        qrStatusValue = valueRow(
            label = "QR code",
            value = "Leitor ativo",
            valueColor = TEXT
        )
        pairingPanel.addView(qrStatusValue.parent as View)
        content.addView(pairingPanel, bottomMargin(dp(18)))

        val codePanel = panel("Codigo / QR")
        deviceNameField = editableRow(
            label = "Nome sugerido",
            hint = "Android Entrega 03"
        )
        deviceNameField.imeOptions = EditorInfo.IME_ACTION_DONE
        codePanel.addView(deviceNameField.parent as View, bottomMargin(dp(7)))

        pairingCodeField = editableRow(
            label = "Codigo manual",
            hint = "PXYY-4XFA"
        )
        pairingCodeField.imeOptions = EditorInfo.IME_ACTION_NEXT
        codePanel.addView(pairingCodeField.parent as View, bottomMargin(dp(7)))

        statePrimaryValue = valueRow("Estado 1", "Aguardando codigo", TEXT)
        codePanel.addView(statePrimaryValue.parent as View, bottomMargin(dp(7)))

        stateDetailValue = valueRow("Estado 2", "Codigo expirado", DANGER)
        codePanel.addView(stateDetailValue.parent as View, bottomMargin(dp(7)))

        stateSuccessValue = valueRow("Estado 3", "Pareado com sucesso", SUCCESS)
        codePanel.addView(stateSuccessValue.parent as View, bottomMargin(dp(7)))

        stateFailureValue = valueRow("Estado 4", "Falha de pareamento", DANGER)
        codePanel.addView(stateFailureValue.parent as View)
        content.addView(codePanel, bottomMargin(dp(18)))

        content.addView(spacer())

        actionButton = commandButton("Validar codigo")
        content.addView(actionButton)

        footerStatus = footer("Pareamento ainda nao concluido")
        root.addView(footerStatus)

        return ScrollView(this).apply {
            setBackgroundColor(SCREEN_BACKGROUND)
            isFillViewport = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(root, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
        }
    }

    private fun submitPairing() {
        val instanceUrl = instanceUrlField.text.toString()
        val pairingCode = pairingCodeField.text.toString()
        val deviceName = deviceNameField.text.toString().ifBlank { "Dispositivo Android" }
        val validating = uiController.validating(instanceUrl, pairingCode)
        render(validating)

        if (validating.stage == PairingStage.FAILURE) {
            return
        }

        actionButton.isEnabled = false
        Thread {
            try {
                val agentInstanceId = agentInstanceId()
                val response = apiClient.complete(
                    instanceUrl,
                    CompletePairingRequest(
                        pairingCode = pairingCode,
                        agentInstanceId = agentInstanceId,
                        platform = "ANDROID",
                        publicKey = keyStore.publicKey(agentInstanceId)
                    )
                )
                getPreferences(MODE_PRIVATE).edit()
                    .putString("paired_device_id", response.deviceId)
                    .putString("paired_device_name", response.deviceName.ifBlank { deviceName })
                    .putString("paired_instance_url", instanceUrl)
                    .apply()

                runOnUiThread {
                    render(uiController.paired(response.deviceName.ifBlank { deviceName }))
                    actionButton.isEnabled = true
                }
            } catch (exception: PairingApiException) {
                runOnUiThread {
                    render(
                        if (exception.expired) {
                            uiController.expired()
                        } else {
                            uiController.failed(exception.userMessage)
                        }
                    )
                    actionButton.isEnabled = true
                }
            } catch (exception: RuntimeException) {
                runOnUiThread {
                    render(uiController.failed("Nao foi possivel conectar com a instancia."))
                    actionButton.isEnabled = true
                }
            }
        }.start()
    }

    private fun render(state: PairingUiState) {
        statusBadge.text = state.badge
        statusBadge.setTextColor(state.color)
        statusBadge.background = bordered(BADGE_BACKGROUND, state.color, dp(1), dp(1))
        apiStatusValue.text = when (state.stage) {
            PairingStage.PAIRED -> "Conectada"
            PairingStage.VALIDATING -> "Conectando"
            PairingStage.FAILURE -> "Falha"
            PairingStage.EXPIRED -> "Codigo expirado"
            PairingStage.WAITING -> "Aguardando"
        }
        apiStatusValue.setTextColor(statusColor(state))
        qrStatusValue.text = when (state.stage) {
            PairingStage.PAIRED -> "Pareado"
            PairingStage.VALIDATING -> "Validando"
            PairingStage.FAILURE -> "Falha"
            PairingStage.EXPIRED -> "Expirado"
            PairingStage.WAITING -> "Leitor ativo"
        }
        qrStatusValue.setTextColor(statusColor(state))
        statePrimaryValue.text = state.title
        statePrimaryValue.setTextColor(state.color)
        stateDetailValue.text = state.detail
        stateDetailValue.setTextColor(
            if (state.stage == PairingStage.EXPIRED || state.stage == PairingStage.FAILURE) {
                DANGER
            } else {
                MUTED
            }
        )
        stateSuccessValue.setTextColor(if (state.stage == PairingStage.PAIRED) SUCCESS else MUTED)
        stateFailureValue.setTextColor(
            if (state.stage == PairingStage.FAILURE || state.stage == PairingStage.EXPIRED) {
                DANGER
            } else {
                MUTED
            }
        )
        footerStatus.text = when (state.stage) {
            PairingStage.PAIRED -> "Pareamento concluido"
            PairingStage.VALIDATING -> "Validando pareamento"
            PairingStage.EXPIRED -> "Codigo expirado"
            PairingStage.FAILURE -> "Pareamento nao concluido"
            PairingStage.WAITING -> "Pareamento ainda nao concluido"
        }
        actionButton.text = when (state.stage) {
            PairingStage.PAIRED -> "Pareado"
            PairingStage.VALIDATING -> "Validando"
            else -> "Validar codigo"
        }
    }

    private fun agentInstanceId(): String {
        val preferences = getPreferences(MODE_PRIVATE)
        val current = preferences.getString("agent_instance_id", null)
        if (!current.isNullOrBlank()) {
            return current
        }

        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val created = "android-${androidId ?: System.currentTimeMillis().toString(16)}"
        preferences.edit().putString("agent_instance_id", created).apply()
        return created
    }

    private fun header(): TextView =
        TextView(this).apply {
            text = "SimpleGuard Agent"
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 13f
            includeFontPadding = false
            gravity = Gravity.CENTER
            setTextColor(ACCENT)
            setBackgroundColor(HEADER_BACKGROUND)
            setPadding(0, dp(18), 0, dp(18))
        }

    private fun panel(title: String): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = bordered(PANEL_BACKGROUND, BORDER, dp(1), dp(2))
            setPadding(dp(14), dp(14), dp(14), dp(14))
            addView(panelTitle(title), bottomMargin(dp(10)))
        }

    private fun panelTitle(text: String): TextView =
        TextView(this).apply {
            this.text = text
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 15f
            includeFontPadding = false
            setTextColor(TEXT)
        }

    private fun badge(text: String): TextView =
        TextView(this).apply {
            this.text = text
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 10f
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(5), dp(8), dp(5))
            background = bordered(BADGE_BACKGROUND, ACCENT, dp(1), dp(1))
            setTextColor(ACCENT)
        }

    private fun editableRow(label: String, hint: String): EditText {
        val container = rowContainer()
        container.addView(rowLabel(label))
        return EditText(this).apply {
            this.hint = hint
            this.contentDescription = label
            typeface = Typeface.MONOSPACE
            textSize = 10f
            includeFontPadding = false
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setHintTextColor(MUTED)
            setTextColor(TEXT)
            setSingleLine(true)
            setPadding(dp(6), 0, 0, 0)
            background = null
            imeOptions = EditorInfo.IME_ACTION_NEXT
            container.addView(
                this,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.3f)
            )
        }
    }

    private fun valueRow(label: String, value: String, valueColor: Int): TextView {
        val container = rowContainer()
        container.addView(rowLabel(label))
        return TextView(this).apply {
            text = value
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 10f
            includeFontPadding = false
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setTextColor(valueColor)
            container.addView(
                this,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.3f)
            )
        }
    }

    private fun rowContainer(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(30)
            background = bordered(ROW_BACKGROUND, ROW_BORDER, dp(1), dp(1))
            setPadding(dp(9), 0, dp(9), 0)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        }

    private fun rowLabel(text: String): TextView =
        TextView(this).apply {
            this.text = text
            typeface = Typeface.MONOSPACE
            textSize = 10f
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(LABEL)
            setPadding(0, 0, dp(8), 0)
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        }

    private fun commandButton(text: String): Button =
        Button(this).apply {
            this.text = text
            typeface = Typeface.MONOSPACE
            textSize = 11f
            isAllCaps = false
            includeFontPadding = false
            setTextColor(TEXT)
            background = bordered(BUTTON_BACKGROUND, ACCENT, dp(1), dp(2))
            setPadding(0, dp(10), 0, dp(10))
        }

    private fun footer(text: String): TextView =
        TextView(this).apply {
            this.text = text
            typeface = Typeface.MONOSPACE
            textSize = 9f
            includeFontPadding = false
            setTextColor(MUTED)
            setBackgroundColor(HEADER_BACKGROUND)
            setPadding(dp(22), dp(8), dp(22), dp(8))
        }

    private fun spacer(): View =
        View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

    private fun statusColor(state: PairingUiState): Int =
        when (state.stage) {
            PairingStage.PAIRED -> SUCCESS
            PairingStage.VALIDATING -> WARNING
            PairingStage.FAILURE,
            PairingStage.EXPIRED -> DANGER
            PairingStage.WAITING -> TEXT
        }

    private fun bordered(backgroundColor: Int, strokeColor: Int, strokeWidth: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(backgroundColor)
            setStroke(strokeWidth, strokeColor)
        }
    }

    private fun bottomMargin(bottomMargin: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, bottomMargin)
        }

    private fun wrapBottomMargin(bottomMargin: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, bottomMargin)
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val SCREEN_BACKGROUND = 0xFF001021.toInt()
        const val HEADER_BACKGROUND = 0xFF000911.toInt()
        const val PANEL_BACKGROUND = 0xFF061923.toInt()
        const val ROW_BACKGROUND = 0xFF071B24.toInt()
        const val BADGE_BACKGROUND = 0xFF062237.toInt()
        const val BUTTON_BACKGROUND = 0xFF053B55.toInt()
        const val BORDER = 0xFF10B8D8.toInt()
        const val ROW_BORDER = 0xFF104554.toInt()
        const val ACCENT = 0xFF3EDCF4.toInt()
        const val TEXT = 0xFFE8FBFF.toInt()
        const val LABEL = 0xFF8CB0BC.toInt()
        const val MUTED = 0xFF6C8791.toInt()
        const val WARNING = 0xFFFFD84D.toInt()
        const val SUCCESS = 0xFF1AFFA9.toInt()
        const val DANGER = 0xFFFF5B5B.toInt()
    }
}
