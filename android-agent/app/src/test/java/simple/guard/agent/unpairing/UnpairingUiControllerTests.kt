package simple.guard.agent.unpairing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnpairingUiControllerTests {
    private val controller = UnpairingUiController()

    @Test
    fun rendersConfirmationRequiredStateTests() {
        val state = controller.confirmationRequired()

        assertEquals(UnpairingStage.CONFIRMATION_REQUIRED, state.stage)
        assertEquals("CONFIRMACAO EXIGIDA", state.badge)
        assertTrue(state.detail.contains("Confirme"))
    }

    @Test
    fun rendersRequestedAndUnpairedStatesTests() {
        val requested = controller.requested()
        val unpaired = controller.unpaired()

        assertEquals(UnpairingStage.REQUESTED, requested.stage)
        assertEquals("Solicitado", requested.primaryState)
        assertEquals(UnpairingStage.UNPAIRED, unpaired.stage)
        assertEquals("Despareado", unpaired.primaryState)
    }

    @Test
    fun rendersRejectedRequestWithoutRemovingPairingTests() {
        val state = controller.rejected()

        assertEquals(UnpairingStage.REJECTED, state.stage)
        assertEquals("SOLICITACAO REJEITADA", state.badge)
        assertTrue(state.detail.contains("rejeitou"))
    }

    @Test
    fun rendersApiFailureWithoutRemovingLocalPairingTests() {
        val state = controller.apiFailure("A instancia recusou a credencial.")

        assertEquals(UnpairingStage.API_FAILURE, state.stage)
        assertEquals("FALHA API", state.badge)
        assertEquals("A instancia recusou a credencial.", state.detail)
    }

    @Test
    fun rendersLocalUnpairingWithPendingSynchronizationTests() {
        val state = controller.syncPending()

        assertEquals(UnpairingStage.SYNC_PENDING, state.stage)
        assertEquals("SYNC PENDENTE", state.badge)
        assertTrue(state.detail.contains("vinculo local foi removido"))
        assertFalse(state.detail.isBlank())
    }
}
