package simple.guard.agent

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.view.Gravity
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
    private lateinit var statusTitle: TextView
    private lateinit var statusDetail: TextView
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
        val fieldSpacing = dp(14)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            minimumHeight = resources.displayMetrics.heightPixels
            setPadding(dp(28), dp(36), dp(28), dp(36))
            setBackgroundColor(Color.rgb(4, 12, 18))
        }

        content.addView(label("SimpleGuard Agent", 28, Color.WHITE))
        content.addView(label("Pareamento do dispositivo", 14, Color.rgb(157, 196, 212)))

        statusBadge = label("", 13, Color.rgb(26, 255, 169))
        statusBadge.setPadding(0, dp(42), 0, dp(8))
        content.addView(statusBadge)

        statusTitle = label("", 22, Color.WHITE)
        content.addView(statusTitle)

        statusDetail = label("", 14, Color.rgb(196, 226, 238))
        statusDetail.setPadding(0, dp(8), 0, dp(30))
        content.addView(statusDetail)

        instanceUrlField = input("URL da instancia", "https://simpleguard.local")
        content.addView(instanceUrlField, fieldLayoutParams(fieldSpacing))

        pairingCodeField = input("Codigo de pareamento", "PXYY-4XFA")
        pairingCodeField.imeOptions = EditorInfo.IME_ACTION_NEXT
        content.addView(pairingCodeField, fieldLayoutParams(fieldSpacing))

        deviceNameField = input("Nome deste dispositivo", "Celular de campo")
        deviceNameField.imeOptions = EditorInfo.IME_ACTION_DONE
        content.addView(deviceNameField, fieldLayoutParams(fieldSpacing))

        content.addView(qrPanel(), fieldLayoutParams(dp(22)))

        actionButton = Button(this).apply {
            text = "Parear dispositivo"
            setTextColor(Color.rgb(3, 18, 25))
            setBackgroundColor(Color.rgb(65, 196, 218))
            setPadding(0, dp(12), 0, dp(12))
        }
        content.addView(actionButton, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        return ScrollView(this).apply {
            setBackgroundColor(Color.rgb(4, 12, 18))
            isFillViewport = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(content, ViewGroup.LayoutParams(
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
        statusTitle.text = state.title
        statusDetail.text = state.detail
        statusBadge.setTextColor(state.color)
        actionButton.text = when (state.stage) {
            PairingStage.PAIRED -> "Pareado"
            PairingStage.VALIDATING -> "Validando"
            else -> "Parear dispositivo"
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

    private fun input(label: String, hint: String): EditText {
        return EditText(this).apply {
            this.hint = hint
            this.contentDescription = label
            setHintTextColor(Color.rgb(134, 152, 160))
            setTextColor(Color.WHITE)
            setSingleLine(true)
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
    }

    private fun label(text: String, size: Int, color: Int): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
        }
    }

    private fun qrPanel(): TextView {
        return TextView(this).apply {
            text = "Leitor de QR code pendente. Use o codigo manual por enquanto."
            textSize = 13f
            setTextColor(Color.rgb(157, 196, 212))
            setPadding(dp(18), dp(22), dp(18), dp(22))
            gravity = Gravity.CENTER
        }
    }

    private fun fieldLayoutParams(bottomMargin: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, bottomMargin)
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
