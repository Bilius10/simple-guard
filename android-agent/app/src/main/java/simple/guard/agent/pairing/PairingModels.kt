package simple.guard.agent.pairing

data class CompletePairingRequest(
    val pairingCode: String,
    val agentInstanceId: String,
    val platform: String,
    val publicKey: String
)

data class CompletePairingResponse(
    val deviceId: String,
    val deviceName: String,
    val pairingStatus: String
)
