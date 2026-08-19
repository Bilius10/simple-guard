package simple.guard.agent.pairing

import kotlin.test.Test
import kotlin.test.assertEquals

class PairingModelsTests {

    @Test
    fun createsCompletePairingRequestWithExpectedFieldsTests() {
        val request = CompletePairingRequest(
            pairingCode = "PXYY-4XFA",
            agentInstanceId = "android-agent-01",
            platform = "ANDROID",
            publicKey = "public-key-value"
        )

        assertEquals("PXYY-4XFA", request.pairingCode)
        assertEquals("android-agent-01", request.agentInstanceId)
        assertEquals("ANDROID", request.platform)
        assertEquals("public-key-value", request.publicKey)
    }

    @Test
    fun createsCompletePairingResponseWithExpectedFieldsTests() {
        val response = CompletePairingResponse(
            deviceId = "00000000-0000-0000-0000-000000000321",
            deviceName = "Moto G34",
            pairingStatus = "PAIRED"
        )

        assertEquals("00000000-0000-0000-0000-000000000321", response.deviceId)
        assertEquals("Moto G34", response.deviceName)
        assertEquals("PAIRED", response.pairingStatus)
    }
}
