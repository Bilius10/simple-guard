package simple.guard.agent.pairing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PairingUiControllerTests {
    private val controller = PairingUiController()

    @Test
    fun startsWaitingForPairingCodeTests() {
        val state = controller.waiting()

        assertEquals(PairingStage.WAITING, state.stage)
        assertEquals("AGUARDANDO CODIGO", state.badge)
        assertTrue(state.detail.contains("codigo curto"))
    }

    @Test
    fun showsValidatingStateWhenRequiredDataExistsTests() {
        val state = controller.validating("https://simpleguard.local", "PXYY-4XFA")

        assertEquals(PairingStage.VALIDATING, state.stage)
        assertEquals("VALIDANDO INSTANCIA", state.badge)
        assertTrue(state.detail.contains("chave local"))
    }

    @Test
    fun showsFailureStateWhenRequiredDataIsMissingTests() {
        val state = controller.validating("", "PXYY-4XFA")

        assertEquals(PairingStage.FAILURE, state.stage)
        assertEquals("FALHA DE PAREAMENTO", state.badge)
        assertEquals("Informe a URL da instancia e o codigo de pareamento.", state.detail)
    }

    @Test
    fun showsFailureStateWhenPairingCodeIsMissingTests() {
        val state = controller.validating("https://simpleguard.local", "")

        assertEquals(PairingStage.FAILURE, state.stage)
        assertEquals("Informe a URL da instancia e o codigo de pareamento.", state.detail)
    }

    @Test
    fun showsExpiredStateForExpiredPairingCodeTests() {
        val state = controller.expired()

        assertEquals(PairingStage.EXPIRED, state.stage)
        assertEquals("CODIGO EXPIRADO", state.badge)
        assertTrue(state.detail.contains("venceu"))
    }

    @Test
    fun showsPairedStateAfterSuccessfulPairingTests() {
        val state = controller.paired("Celular operacional")

        assertEquals(PairingStage.PAIRED, state.stage)
        assertEquals("PAREADO", state.badge)
        assertEquals("Celular operacional", state.title)
    }
}
