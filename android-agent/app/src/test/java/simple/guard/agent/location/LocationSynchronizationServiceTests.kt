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

    @Test
    fun collectsAndSendsValidLocationTests() {
        var sentReading: LocationReading? = null
        val service = service(
            LocationCollectionResult.Collected(reading),
            LocationSendResult.Sent,
            onSend = { sentReading = it }
        )

        var result: LocationSynchronizationResult? = null
        service.synchronize { result = it }

        assertEquals(reading, sentReading)
        assertEquals(LocationSynchronizationResult.Sent, result)
    }

    @Test
    fun handlesDeniedPermissionWithoutSendingTests() {
        assertCollectionFailureTests(
            LocationCollectionResult.PermissionDenied,
            LocationSynchronizationResult.PermissionDenied
        )
    }

    @Test
    fun handlesUnavailableGpsWithoutSendingTests() {
        assertCollectionFailureTests(
            LocationCollectionResult.ProviderUnavailable,
            LocationSynchronizationResult.ProviderUnavailable
        )
    }

    @Test
    fun handlesAbsentLocationWithoutSendingTests() {
        assertCollectionFailureTests(
            LocationCollectionResult.LocationUnavailable,
            LocationSynchronizationResult.LocationUnavailable
        )
    }

    @Test
    fun handlesSendFailureTests() {
        val service = service(LocationCollectionResult.Collected(reading), LocationSendResult.Failed)

        var result: LocationSynchronizationResult? = null
        service.synchronize { result = it }

        assertEquals(LocationSynchronizationResult.SendFailure, result)
    }

    private fun assertCollectionFailureTests(
        collectionResult: LocationCollectionResult,
        expected: LocationSynchronizationResult
    ) {
        var sendCount = 0
        val service = service(collectionResult, LocationSendResult.Sent, onSend = { sendCount++ })

        var result: LocationSynchronizationResult? = null
        service.synchronize { result = it }

        assertEquals(0, sendCount)
        assertEquals(expected, result)
    }

    private fun service(
        collectionResult: LocationCollectionResult,
        sendResult: LocationSendResult,
        onSend: (LocationReading) -> Unit = {}
    ): LocationSynchronizationService {
        return LocationSynchronizationService(
            collector = LocationCollector { callback -> callback(collectionResult) },
            sender = LocationSender { value, callback ->
                onSend(value)
                callback(sendResult)
            }
        )
    }
}
