package simple.guard.agent.welcome

import kotlin.test.Test
import kotlin.test.assertEquals

class WelcomeUiControllerTests {
    private val controller = WelcomeUiController()

    @Test
    fun rendersWelcomeContentTests() {
        val state = controller.welcome()

        assertEquals("Agente SimpleGuard", state.title)
        assertEquals("NAO PAREADO", state.status)
        assertEquals(
            listOf(
                WelcomeSummaryItem("Funcao", "Conectar este dispositivo"),
                WelcomeSummaryItem("Escopo", "Telemetria e comandos"),
                WelcomeSummaryItem("Promessa", "Sem recuperacao garantida", warning = true),
            ),
            state.summary,
        )
        assertEquals("Funcao do agente", state.capabilitiesTitle)
        assertEquals(
            listOf(
                "Envia localizacao e telemetria",
                "Recebe comandos suportados",
                "Sincroniza eventos ao reconectar",
            ),
            state.capabilities,
        )
        assertEquals("Iniciar pareamento", state.actionLabel)
        assertEquals("Pronto para iniciar pareamento", state.footer)
    }

    @Test
    fun startsAtWelcomeWhenDeviceHasNoPairingTests() {
        assertEquals(AgentScreen.WELCOME, controller.initialScreen(hasLocalPairing = false))
    }

    @Test
    fun startsAtPairedScreenWhenDeviceHasLocalPairingTests() {
        assertEquals(AgentScreen.PAIRED, controller.initialScreen(hasLocalPairing = true))
    }

    @Test
    fun startPairingActionChangesToPairingScreenTests() {
        val initialScreen = controller.initialScreen(hasLocalPairing = false)
        val destination = controller.startPairing()

        assertEquals(AgentScreen.WELCOME, initialScreen)
        assertEquals(AgentScreen.PAIRING, destination)
    }
}
