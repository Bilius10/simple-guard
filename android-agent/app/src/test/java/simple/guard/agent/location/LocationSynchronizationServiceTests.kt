package simple.guard.agent.location

import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationSynchronizationServiceTests {

    private val reading = LocationReading(
        latitude = BigDecimal("-23.550520"),
        longitude = BigDecimal("-46.633308"),
        accuracyMeters = BigDecimal("4.500"),
        altitudeMeters = null,
        speedMetersPerSecond = null,
        provider = "GPS",
        collectedAt = Instant.parse("2026-08-17T12:00:00Z")
    )

    private val technical = TechnicalTelemetryReading(
        batteryLevelPercentage = 67,
        batteryCharging = false,
        networkType = NetworkType.WIFI,
        signalStrengthDbm = -55,
        permissions = TelemetryPermissions(PermissionState.GRANTED, PermissionState.GRANTED),
        collectedAt = Instant.parse("2026-08-17T12:00:02Z")
    )

    @Test
    fun collectsAndSendsValidLocationTests() {
        var sentEnvelope: TelemetryEnvelope? = null
        val service = service(
            LocationCollectionResult.Collected(reading),
            technical,
            TelemetrySendResult.Sent,
            onSend = { sentEnvelope = it }
        )

        var result: LocationSynchronizationResult? = null
        service.synchronize { result = it }

        assertEquals(reading, sentEnvelope?.location)
        assertEquals(technical, sentEnvelope?.technical)
        assertEquals("00000000-0000-0000-0000-000000000905", sentEnvelope?.eventId)
        assertEquals(LocationSynchronizationResult.Sent(LocationCollectionStatus.COLLECTED), result)
    }

    @Test
    fun sendsAbsentTechnicalValuesAndPermissionStatusTests() {
        val absent = TechnicalTelemetryReading(null, null, null, null, null, technical.collectedAt)
        var sentEnvelope: TelemetryEnvelope? = null
        val service = service(
            LocationCollectionResult.PermissionDenied,
            absent,
            TelemetrySendResult.Sent,
            onSend = { sentEnvelope = it }
        )

        var result: LocationSynchronizationResult? = null
        service.synchronize { result = it }

        assertEquals(null, sentEnvelope?.location)
        assertEquals(absent, sentEnvelope?.technical)
        assertEquals(LocationSynchronizationResult.Sent(LocationCollectionStatus.PERMISSION_DENIED), result)
    }

    @Test
    fun sendsLowBatteryWithoutLocationTests() {
        val lowBattery = technical.copy(batteryLevelPercentage = 5, signalStrengthDbm = null)
        var sentEnvelope: TelemetryEnvelope? = null
        val service = service(
            LocationCollectionResult.ProviderUnavailable,
            lowBattery,
            TelemetrySendResult.Sent,
            onSend = { sentEnvelope = it }
        )

        var result: LocationSynchronizationResult? = null
        service.synchronize { result = it }

        assertEquals(5, sentEnvelope?.technical?.batteryLevelPercentage)
        assertEquals(LocationSynchronizationResult.Sent(LocationCollectionStatus.PROVIDER_UNAVAILABLE), result)
    }

    @Test
    fun handlesSendFailureTests() {
        val service = service(
            LocationCollectionResult.LocationUnavailable,
            technical,
            TelemetrySendResult.Failed
        )

        var result: LocationSynchronizationResult? = null
        service.synchronize { result = it }

        assertEquals(LocationSynchronizationResult.SendFailure(LocationCollectionStatus.LOCATION_UNAVAILABLE), result)
    }

    private fun service(
        collectionResult: LocationCollectionResult,
        technicalReading: TechnicalTelemetryReading,
        sendResult: TelemetrySendResult,
        onSend: (TelemetryEnvelope) -> Unit = {}
    ): LocationSynchronizationService {
        return LocationSynchronizationService(
            collector = LocationCollector { callback -> callback(collectionResult) },
            technicalCollector = TechnicalTelemetryCollector { technicalReading },
            sender = LocationSender { envelope, callback ->
                onSend(envelope)
                callback(sendResult)
            },
            eventIdProvider = { "00000000-0000-0000-0000-000000000905" }
        )
    }
}
