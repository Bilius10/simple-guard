package simple.guard.agent.pairing

enum class PairingStage {
    WAITING,
    VALIDATING,
    EXPIRED,
    FAILURE,
    PAIRED,
}

data class PairingUiState(
    val stage: PairingStage,
    val badge: String,
    val title: String,
    val detail: String,
    val color: Int,
)

class PairingUiController {
    fun waiting(): PairingUiState =
        PairingUiState(
            stage = PairingStage.WAITING,
            badge = "AGUARDANDO CODIGO",
            title = "Informe o codigo da web",
            detail = "Use o codigo curto gerado para este dispositivo na instancia SimpleGuard.",
            color = 0xFFF6C44D.toInt(),
        )

    fun validating(
        instanceUrl: String,
        pairingCode: String,
    ): PairingUiState {
        if (instanceUrl.isBlank() || pairingCode.isBlank()) {
            return failed("Informe a URL da instancia e o codigo de pareamento.")
        }

        return PairingUiState(
            stage = PairingStage.VALIDATING,
            badge = "VALIDANDO INSTANCIA",
            title = "Conectando com a API",
            detail = "A chave local esta sendo preparada e enviada para autorizacao.",
            color = 0xFF48CDEB.toInt(),
        )
    }

    fun expired(): PairingUiState =
        PairingUiState(
            stage = PairingStage.EXPIRED,
            badge = "CODIGO EXPIRADO",
            title = "Gere um novo codigo",
            detail = "O codigo informado venceu ou ja nao esta aguardando pareamento.",
            color = 0xFFF6C44D.toInt(),
        )

    fun failed(message: String): PairingUiState =
        PairingUiState(
            stage = PairingStage.FAILURE,
            badge = "FALHA DE PAREAMENTO",
            title = "Nao foi possivel parear",
            detail = message,
            color = 0xFFFF5B5B.toInt(),
        )

    fun paired(deviceName: String): PairingUiState =
        PairingUiState(
            stage = PairingStage.PAIRED,
            badge = "PAREADO",
            title = deviceName,
            detail = "Este dispositivo esta vinculado e pronto para enviar telemetria autorizada.",
            color = 0xFF1AFFA9.toInt(),
        )
}
