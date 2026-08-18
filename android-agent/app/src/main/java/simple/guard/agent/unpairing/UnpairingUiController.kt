package simple.guard.agent.unpairing

enum class UnpairingStage {
    CONFIRMATION_REQUIRED,
    REQUESTED,
    UNPAIRED,
    REJECTED,
    API_FAILURE,
    SYNC_PENDING
}

data class UnpairingUiState(
    val stage: UnpairingStage,
    val badge: String,
    val primaryState: String,
    val detail: String,
    val color: Int
)

class UnpairingUiController {

    fun confirmationRequired(): UnpairingUiState = UnpairingUiState(
        stage = UnpairingStage.CONFIRMATION_REQUIRED,
        badge = "CONFIRMACAO EXIGIDA",
        primaryState = "Aguardando confirmacao",
        detail = "Confirme para remover o vinculo deste dispositivo.",
        color = 0xFFFF5B5B.toInt()
    )

    fun requested(): UnpairingUiState = UnpairingUiState(
        stage = UnpairingStage.REQUESTED,
        badge = "DESPAREAMENTO SOLICITADO",
        primaryState = "Solicitado",
        detail = "A instancia esta revogando as credenciais do agente.",
        color = 0xFFFFD84D.toInt()
    )

    fun unpaired(): UnpairingUiState = UnpairingUiState(
        stage = UnpairingStage.UNPAIRED,
        badge = "DESPAREADO",
        primaryState = "Despareado",
        detail = "O vinculo e as credenciais locais foram removidos.",
        color = 0xFF1AFFA9.toInt()
    )

    fun rejected(): UnpairingUiState = UnpairingUiState(
        stage = UnpairingStage.REJECTED,
        badge = "SOLICITACAO REJEITADA",
        primaryState = "Vinculo mantido",
        detail = "O administrador rejeitou a solicitacao de despareamento.",
        color = 0xFFFF5B5B.toInt()
    )

    fun apiFailure(message: String): UnpairingUiState = UnpairingUiState(
        stage = UnpairingStage.API_FAILURE,
        badge = "FALHA API",
        primaryState = "Falha API",
        detail = message,
        color = 0xFFFF5B5B.toInt()
    )

    fun syncPending(): UnpairingUiState = UnpairingUiState(
        stage = UnpairingStage.SYNC_PENDING,
        badge = "SYNC PENDENTE",
        primaryState = "Sync pendente",
        detail = "O vinculo local foi removido. A revogacao sera reenviada quando houver conexao.",
        color = 0xFFFFD84D.toInt()
    )
}
