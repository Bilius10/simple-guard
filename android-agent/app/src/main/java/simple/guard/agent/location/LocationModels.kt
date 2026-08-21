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

enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    OTHER
}

enum class PermissionState {
    GRANTED,
    DENIED
}

data class TelemetryPermissions(
    val fineLocation: PermissionState?,
    val coarseLocation: PermissionState?
)

data class TechnicalTelemetryReading(
    val batteryLevelPercentage: Int?,
    val batteryCharging: Boolean?,
    val networkType: NetworkType?,
    val signalStrengthDbm: Int?,
    val permissions: TelemetryPermissions?,
    val collectedAt: Instant
)

enum class LocationCollectionStatus {
    COLLECTED,
    PERMISSION_DENIED,
    PROVIDER_UNAVAILABLE,
    LOCATION_UNAVAILABLE
}

data class TelemetryEnvelope(
    val eventId: String,
    val location: LocationReading?,
    val technical: TechnicalTelemetryReading,
    val locationStatus: LocationCollectionStatus
)

sealed interface LocationCollectionResult {
    data class Collected(val reading: LocationReading) : LocationCollectionResult
    data object PermissionDenied : LocationCollectionResult
    data object ProviderUnavailable : LocationCollectionResult
    data object LocationUnavailable : LocationCollectionResult
}

sealed interface TelemetrySendResult {
    data object Sent : TelemetrySendResult
    data object Failed : TelemetrySendResult
}

sealed interface LocationSynchronizationResult {
    data class Sent(val locationStatus: LocationCollectionStatus) : LocationSynchronizationResult
    data class SendFailure(val locationStatus: LocationCollectionStatus) : LocationSynchronizationResult
}

fun interface LocationCollector {
    fun collect(callback: (LocationCollectionResult) -> Unit)
}

fun interface TechnicalTelemetryCollector {
    fun collect(): TechnicalTelemetryReading
}

fun interface LocationSender {
    fun send(envelope: TelemetryEnvelope, callback: (TelemetrySendResult) -> Unit)
}

object TelemetrySignaturePayload {

    fun bytes(deviceId: String, agentInstanceId: String, envelope: TelemetryEnvelope): ByteArray {
        val location = envelope.location
        val technical = envelope.technical
        return listOf(
            "INGEST_TELEMETRY",
            deviceId,
            agentInstanceId,
            envelope.eventId,
            if (location == null) "0" else "1",
            location?.collectedAt?.toString().orEmpty(),
            canonical(location?.latitude),
            canonical(location?.longitude),
            canonical(location?.accuracyMeters),
            canonical(location?.altitudeMeters),
            canonical(location?.speedMetersPerSecond),
            location?.provider.orEmpty(),
            "1",
            technical.collectedAt.toString(),
            technical.batteryLevelPercentage?.toString().orEmpty(),
            technical.batteryCharging?.toString().orEmpty(),
            technical.networkType?.name.orEmpty(),
            technical.signalStrengthDbm?.toString().orEmpty(),
            technical.permissions?.fineLocation?.name.orEmpty(),
            technical.permissions?.coarseLocation?.name.orEmpty()
        ).joinToString("\n").toByteArray(Charsets.UTF_8)
    }

    private fun canonical(value: BigDecimal?): String {
        return value?.stripTrailingZeros()?.toPlainString().orEmpty()
    }
}
