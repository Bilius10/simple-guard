package simple.guard.agent.location

import java.math.BigDecimal
import java.time.Instant

data class LocationReading(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val accuracyMeters: BigDecimal?,
    val altitudeMeters: BigDecimal?,
    val speedMetersPerSecond: BigDecimal?,
    val provider: String,
    val collectedAt: Instant
)

sealed interface LocationCollectionResult {
    data class Collected(val reading: LocationReading) : LocationCollectionResult
    data object PermissionDenied : LocationCollectionResult
    data object ProviderUnavailable : LocationCollectionResult
    data object LocationUnavailable : LocationCollectionResult
}

sealed interface LocationSendResult {
    data object Sent : LocationSendResult
    data object Failed : LocationSendResult
}

sealed interface LocationSynchronizationResult {
    data object Sent : LocationSynchronizationResult
    data object PermissionDenied : LocationSynchronizationResult
    data object ProviderUnavailable : LocationSynchronizationResult
    data object LocationUnavailable : LocationSynchronizationResult
    data object SendFailure : LocationSynchronizationResult
}

fun interface LocationCollector {
    fun collect(callback: (LocationCollectionResult) -> Unit)
}

fun interface LocationSender {
    fun send(reading: LocationReading, callback: (LocationSendResult) -> Unit)
}

object LocationSignaturePayload {

    fun bytes(deviceId: String, agentInstanceId: String, reading: LocationReading): ByteArray {
        return listOf(
            "INGEST_LOCATION",
            deviceId,
            agentInstanceId,
            reading.collectedAt.toString(),
            canonical(reading.latitude),
            canonical(reading.longitude),
            canonical(reading.accuracyMeters),
            canonical(reading.altitudeMeters),
            canonical(reading.speedMetersPerSecond),
            reading.provider
        ).joinToString("\n").toByteArray(Charsets.UTF_8)
    }

    private fun canonical(value: BigDecimal?): String {
        return value?.stripTrailingZeros()?.toPlainString().orEmpty()
    }
}
