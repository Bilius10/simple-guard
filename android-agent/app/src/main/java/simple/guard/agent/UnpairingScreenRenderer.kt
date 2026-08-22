package simple.guard.agent

import simple.guard.agent.unpairing.UnpairingStage
import simple.guard.agent.unpairing.UnpairingUiState

internal class UnpairingScreenRenderer {
    fun render(
        views: UnpairingScreenViews,
        state: UnpairingUiState,
    ) {
        renderBadge(views, state)
        renderStateValues(views, state)
        renderFooter(views, state.stage)
        renderAction(views, state.stage)
    }

    private fun renderBadge(
        views: UnpairingScreenViews,
        state: UnpairingUiState,
    ) {
        views.statusBadge.text = state.badge
        views.statusBadge.setTextColor(state.color)
        views.statusBadge.background =
            AgentScreenTheme.bordered(
                AgentScreenTheme.BADGE_BACKGROUND,
                state.color,
                1,
                1,
            )
    }

    private fun renderStateValues(
        views: UnpairingScreenViews,
        state: UnpairingUiState,
    ) {
        views.detailValue.text = state.detail
        views.detailValue.setTextColor(state.color)
        views.requestedValue.setTextColor(stageColor(state.stage, UnpairingStage.REQUESTED, AgentScreenTheme.WARNING))
        views.unpairedValue.setTextColor(stageColor(state.stage, UnpairingStage.UNPAIRED, AgentScreenTheme.SUCCESS))
        views.failureValue.setTextColor(stageColor(state.stage, UnpairingStage.API_FAILURE, AgentScreenTheme.DANGER))
        views.pendingValue.setTextColor(stageColor(state.stage, UnpairingStage.SYNC_PENDING, AgentScreenTheme.WARNING))
    }

    private fun renderFooter(
        views: UnpairingScreenViews,
        stage: UnpairingStage,
    ) {
        views.footerStatus.text =
            when (stage) {
                UnpairingStage.CONFIRMATION_REQUIRED -> "Nenhuma alteracao aplicada"
                UnpairingStage.REQUESTED -> "Solicitacao aguardando admin"
                UnpairingStage.UNPAIRED -> "Vinculo removido"
                UnpairingStage.REJECTED -> "Vinculo mantido"
                UnpairingStage.API_FAILURE -> "Vinculo mantido"
                UnpairingStage.SYNC_PENDING -> "Aguardando conexao"
            }
    }

    private fun renderAction(
        views: UnpairingScreenViews,
        stage: UnpairingStage,
    ) {
        views.actionButton.text =
            when (stage) {
                UnpairingStage.REQUESTED -> "Reenviar solicitacao"
                UnpairingStage.SYNC_PENDING -> "Tentar sincronizar"
                UnpairingStage.API_FAILURE -> "Tentar novamente"
                UnpairingStage.UNPAIRED -> "Iniciar novo pareamento"
                UnpairingStage.REJECTED -> "Solicitar novamente"
                UnpairingStage.CONFIRMATION_REQUIRED -> "Desparear dispositivo"
            }
    }

    private fun stageColor(
        currentStage: UnpairingStage,
        expectedStage: UnpairingStage,
        activeColor: Int,
    ): Int =
        if (currentStage == expectedStage) {
            activeColor
        } else {
            AgentScreenTheme.MUTED
        }
}
