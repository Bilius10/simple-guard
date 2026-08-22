package simple.guard.agent

import android.app.Activity
import android.widget.ScrollView
import simple.guard.agent.welcome.WelcomeUiState

internal class WelcomeScreenFactory(
    activity: Activity,
) : BaseScreenFactory(activity) {
    fun build(
        state: WelcomeUiState,
        onStartPairing: () -> Unit,
    ): ScrollView {
        val root = screenRoot()
        val content = contentRoot(paddingTop = 28)
        root.addView(header())
        root.addView(content.view, content.layoutParams)

        content.view.addView(buildSummaryPanel(state), bottomMargin(dp(22)))
        content.view.addView(buildCapabilitiesPanel(state), bottomMargin(dp(20)))

        val startPairingButton =
            commandButton(state.actionLabel).apply {
                minimumHeight = dp(36)
                setPadding(0, 0, 0, 0)
                setOnClickListener { onStartPairing() }
            }
        content.view.addView(startPairingButton)
        content.view.addView(spacer())
        root.addView(footer(state.footer))
        return scrollView(root)
    }

    private fun buildSummaryPanel(state: WelcomeUiState) =
        panel(state.title).apply {
            addView(welcomeStatusBadge(state.status), wrapBottomMargin(dp(10)))
            state.summary.forEachIndexed { index, item ->
                addView(
                    welcomeSummaryRow(item),
                    bottomMargin(if (index == state.summary.lastIndex) 0 else dp(7)),
                )
            }
        }

    private fun buildCapabilitiesPanel(state: WelcomeUiState) =
        panel(state.capabilitiesTitle).apply {
            minimumHeight = dp(320)
            state.capabilities.forEachIndexed { index, capability ->
                addView(
                    numberedCapabilityRow(index + 1, capability),
                    bottomMargin(if (index == state.capabilities.lastIndex) 0 else dp(7)),
                )
            }
        }
}
