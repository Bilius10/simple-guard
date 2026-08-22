package simple.guard.agent.welcome

enum class AgentScreen {
    WELCOME,
    PAIRING,
    PAIRED,
}

data class WelcomeUiState(
    val title: String,
    val status: String,
    val summary: List<WelcomeSummaryItem>,
    val capabilitiesTitle: String,
    val capabilities: List<String>,
    val actionLabel: String,
    val footer: String,
)

data class WelcomeSummaryItem(
    val label: String,
    val value: String,
    val warning: Boolean = false,
)

class WelcomeUiController {
    fun initialScreen(hasLocalPairing: Boolean): AgentScreen = if (hasLocalPairing) AgentScreen.PAIRED else AgentScreen.WELCOME

    fun welcome(): WelcomeUiState =
        WelcomeUiState(
            title = "Agente SimpleGuard",
            status = "NAO PAREADO",
            summary =
                listOf(
                    WelcomeSummaryItem("Funcao", "Conectar este dispositivo"),
                    WelcomeSummaryItem("Escopo", "Telemetria e comandos"),
                    WelcomeSummaryItem("Promessa", "Sem recuperacao garantida", warning = true),
                ),
            capabilitiesTitle = "Funcao do agente",
            capabilities =
                listOf(
                    "Envia localizacao e telemetria",
                    "Recebe comandos suportados",
                    "Sincroniza eventos ao reconectar",
                ),
            actionLabel = "Iniciar pareamento",
            footer = "Pronto para iniciar pareamento",
        )

    fun startPairing(): AgentScreen = AgentScreen.PAIRING
}
