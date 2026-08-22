package simple.guard.agent

import simple.guard.agent.pairing.PairingStage
import simple.guard.agent.pairing.PairingUiState

internal class PairingScreenRenderer {
    fun render(
        views: PairingScreenViews,
        state: PairingUiState,
    ) {
        renderBadge(views, state)
        renderConnectionStatuses(views, state)
        renderStateValues(views, state)
        renderFooter(views, state)
        renderAction(views, state)
    }

    private fun renderBadge(
        views: PairingScreenViews,
        state: PairingUiState,
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

    private fun renderConnectionStatuses(
        views: PairingScreenViews,
        state: PairingUiState,
    ) {
        views.apiStatusValue.text = apiStatus(state.stage)
        views.apiStatusValue.setTextColor(statusColor(state.stage))
        views.qrStatusValue.text = qrStatus(state.stage)
        views.qrStatusValue.setTextColor(statusColor(state.stage))
    }

    private fun renderStateValues(
        views: PairingScreenViews,
        state: PairingUiState,
    ) {
        views.statePrimaryValue.text = state.title
        views.statePrimaryValue.setTextColor(state.color)
        views.stateDetailValue.text = state.detail
        views.stateDetailValue.setTextColor(detailColor(state.stage))
        views.stateSuccessValue.setTextColor(successColor(state.stage))
        views.stateFailureValue.setTextColor(failureColor(state.stage))
    }

    private fun renderFooter(
        views: PairingScreenViews,
        state: PairingUiState,
    ) {
        views.footerStatus.text =
            when (state.stage) {
                PairingStage.PAIRED -> "Pareamento concluido"
                PairingStage.VALIDATING -> "Validando pareamento"
                PairingStage.EXPIRED -> "Codigo expirado"
                PairingStage.FAILURE -> "Pareamento nao concluido"
                PairingStage.WAITING -> "Pareamento ainda nao concluido"
            }
    }

    private fun renderAction(
        views: PairingScreenViews,
        state: PairingUiState,
    ) {
        views.actionButton.text =
            when (state.stage) {
                PairingStage.PAIRED -> "Pareado"
                PairingStage.VALIDATING -> "Validando"
                else -> "Validar codigo"
            }
    }

    private fun apiStatus(stage: PairingStage): String =
        when (stage) {
            PairingStage.PAIRED -> "Conectada"
            PairingStage.VALIDATING -> "Conectando"
            PairingStage.FAILURE -> "Falha"
            PairingStage.EXPIRED -> "Codigo expirado"
            PairingStage.WAITING -> "Aguardando"
        }

    private fun qrStatus(stage: PairingStage): String =
        when (stage) {
            PairingStage.PAIRED -> "Pareado"
            PairingStage.VALIDATING -> "Validando"
            PairingStage.FAILURE -> "Falha"
            PairingStage.EXPIRED -> "Expirado"
            PairingStage.WAITING -> "Leitor ativo"
        }

    private fun detailColor(stage: PairingStage): Int =
        when (stage) {
            PairingStage.EXPIRED,
            PairingStage.FAILURE,
            -> AgentScreenTheme.DANGER
            else -> AgentScreenTheme.MUTED
        }

    private fun successColor(stage: PairingStage): Int =
        if (stage == PairingStage.PAIRED) {
            AgentScreenTheme.SUCCESS
        } else {
            AgentScreenTheme.MUTED
        }

    private fun failureColor(stage: PairingStage): Int =
        when (stage) {
            PairingStage.FAILURE,
            PairingStage.EXPIRED,
            -> AgentScreenTheme.DANGER
            else -> AgentScreenTheme.MUTED
        }

    private fun statusColor(stage: PairingStage): Int =
        when (stage) {
            PairingStage.PAIRED -> AgentScreenTheme.SUCCESS
            PairingStage.VALIDATING -> AgentScreenTheme.WARNING
            PairingStage.FAILURE,
            PairingStage.EXPIRED,
            -> AgentScreenTheme.DANGER
            PairingStage.WAITING -> AgentScreenTheme.TEXT
        }
}
