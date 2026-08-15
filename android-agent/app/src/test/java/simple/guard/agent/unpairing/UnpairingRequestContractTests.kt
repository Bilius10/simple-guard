package simple.guard.agent.unpairing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnpairingRequestContractTests {

    @Test
    fun acceptsPendingUnpairingRequestAsSuccessfulAgentResponseTests() {
        val response = DeviceUnpairingRequestResponse(
            requestId = "request-001",
            deviceId = "device-001",
            deviceName = "Celular operacional",
            agentInstanceId = "android-agent-001",
            status = "pending"
        )

        val accepted = UnpairingRequestContract.requirePendingRequest(response)

        assertEquals(response, accepted)
    }

    @Test
    fun rejectsUnexpectedUnpairingRequestStatusTests() {
        val response = DeviceUnpairingRequestResponse(
            requestId = "request-001",
            deviceId = "device-001",
            deviceName = "Celular operacional",
            agentInstanceId = "android-agent-001",
            status = "approved"
        )

        val exception = assertFailsWith<UnpairingApiException> {
            UnpairingRequestContract.requirePendingRequest(response)
        }

        assertEquals("A instancia retornou um estado de despareamento inesperado.", exception.userMessage)
    }
}
