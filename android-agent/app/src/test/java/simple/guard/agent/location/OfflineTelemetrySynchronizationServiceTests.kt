package simple.guard.agent.location

import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineTelemetrySynchronizationServiceTests {
    private val now = Instant.parse("2026-08-20T12:00:00Z")
    private val technical =
        TechnicalTelemetryReading(
            batteryLevelPercentage = 67,
            batteryCharging = false,
            networkType = NetworkType.WIFI,
            signalStrengthDbm = -55,
            permissions = TelemetryPermissions(PermissionState.GRANTED, PermissionState.GRANTED),
            collectedAt = now,
        )

    @Test
    fun enqueuesBeforeSendingAndCleansAcceptedEventTests() {
        val queue = InMemoryQueue()
        val sender =
            RecordingSender(
                TelemetryBatchSendResult.Completed(
                    listOf(TelemetryBatchItemResult("current", TelemetryBatchItemStatus.ACCEPTED, null)),
                ),
            )
        val service =
            service(
                collection = LocationCollectionResult.Collected(location(now.minusSeconds(2))),
                queue = queue,
                sender = sender,
            )

        var result: LocationSynchronizationResult? = null
        service.synchronize { result = it }

        assertEquals(listOf("current"), sender.sentBatches.single())
        assertEquals(0, queue.size(now))
        assertEquals(LocationSynchronizationResult.Sent(LocationCollectionStatus.COLLECTED), result)
    }

    @Test
    fun keepsFailedEventAndRetriesItAfterReconnectTests() {
        val queue = InMemoryQueue()
        val sender =
            RecordingSender(
                TelemetryBatchSendResult.Failed("offline"),
                TelemetryBatchSendResult.Completed(
                    listOf(TelemetryBatchItemResult("current", TelemetryBatchItemStatus.ACCEPTED, null)),
                ),
            )
        val service = service(LocationCollectionResult.LocationUnavailable, queue, sender)

        var firstResult: LocationSynchronizationResult? = null
        service.synchronize { firstResult = it }

        assertEquals(LocationSynchronizationResult.SendFailure(LocationCollectionStatus.LOCATION_UNAVAILABLE, 1), firstResult)
        assertEquals(1, queue.pending(100, now).single().attemptCount)

        var retryResult: LocationSynchronizationResult? = null
        service.retryPending { retryResult = it }

        assertEquals(LocationSynchronizationResult.Sent(null), retryResult)
        assertEquals(0, queue.size(now))
        assertEquals(2, sender.sentBatches.size)
    }

    @Test
    fun removesTerminalResultsAndKeepsRetryableOrMissingResultsTests() {
        val queue = InMemoryQueue()
        listOf("accepted", "duplicate", "invalid", "unauthorized", "failed", "missing").forEachIndexed { index, id ->
            queue.enqueue(envelope(id, now.plusSeconds(index.toLong())), now)
        }
        val sender =
            RecordingSender(
                TelemetryBatchSendResult.Completed(
                    listOf(
                        TelemetryBatchItemResult(null, TelemetryBatchItemStatus.INVALID, "unmapped"),
                        TelemetryBatchItemResult("accepted", TelemetryBatchItemStatus.ACCEPTED, null),
                        TelemetryBatchItemResult("duplicate", TelemetryBatchItemStatus.DUPLICATE, null),
                        TelemetryBatchItemResult("invalid", TelemetryBatchItemStatus.INVALID, "invalid"),
                        TelemetryBatchItemResult("unauthorized", TelemetryBatchItemStatus.UNAUTHORIZED, "credential"),
                        TelemetryBatchItemResult("failed", TelemetryBatchItemStatus.FAILED, null),
                    ),
                ),
                TelemetryBatchSendResult.Completed(
                    listOf(
                        TelemetryBatchItemResult("unauthorized", TelemetryBatchItemStatus.ACCEPTED, null),
                        TelemetryBatchItemResult("failed", TelemetryBatchItemStatus.ACCEPTED, null),
                        TelemetryBatchItemResult("missing", TelemetryBatchItemStatus.ACCEPTED, null),
                    ),
                ),
            )
        val service = service(LocationCollectionResult.PermissionDenied, queue, sender)

        var firstResult: LocationSynchronizationResult? = null
        service.retryPending { firstResult = it }

        assertEquals(LocationSynchronizationResult.SendFailure(null, 3), firstResult)
        assertEquals(listOf("unauthorized", "failed", "missing"), queue.pending(100, now).map { it.envelope.eventId })
        assertTrue(queue.pending(100, now).all { it.attemptCount == 1 })

        var retryResult: LocationSynchronizationResult? = null
        service.retryPending { retryResult = it }

        assertEquals(LocationSynchronizationResult.Sent(null), retryResult)
        assertEquals(0, queue.size(now))
    }

    @Test
    fun sendsTelemetryForPermissionAndProviderFailuresTests() {
        val statuses =
            listOf(
                LocationCollectionResult.PermissionDenied to LocationCollectionStatus.PERMISSION_DENIED,
                LocationCollectionResult.ProviderUnavailable to LocationCollectionStatus.PROVIDER_UNAVAILABLE,
            )

        statuses.forEachIndexed { index, (collection, expectedStatus) ->
            val eventId = "status-$index"
            val queue = InMemoryQueue()
            val sender =
                RecordingSender(
                    TelemetryBatchSendResult.Completed(
                        listOf(TelemetryBatchItemResult(eventId, TelemetryBatchItemStatus.ACCEPTED, null)),
                    ),
                )
            val service = service(collection, queue, sender, eventId)

            var result: LocationSynchronizationResult? = null
            service.synchronize { result = it }

            assertEquals(LocationSynchronizationResult.Sent(expectedStatus), result)
        }
    }

    private fun service(
        collection: LocationCollectionResult,
        queue: TelemetryOfflineQueue,
        sender: TelemetryBatchSender,
        eventId: String = "current",
    ): OfflineTelemetrySynchronizationService {
        return OfflineTelemetrySynchronizationService(
            collector = LocationCollector { callback -> callback(collection) },
            technicalCollector = TechnicalTelemetryCollector { technical },
            sender = sender,
            queue = queue,
            eventIdProvider = { eventId },
            nowProvider = { now },
        )
    }

    private fun location(collectedAt: Instant): LocationReading {
        return LocationReading(
            latitude = BigDecimal("-23.55052"),
            longitude = BigDecimal("-46.633308"),
            accuracyMeters = BigDecimal("4.5"),
            altitudeMeters = null,
            speedMetersPerSecond = null,
            provider = "GPS",
            collectedAt = collectedAt,
        )
    }

    private fun envelope(
        eventId: String,
        collectedAt: Instant,
    ): TelemetryEnvelope {
        return TelemetryEnvelope(
            eventId,
            null,
            technical.copy(collectedAt = collectedAt),
            LocationCollectionStatus.LOCATION_UNAVAILABLE,
        )
    }

    private class RecordingSender(vararg responses: TelemetryBatchSendResult) : TelemetryBatchSender {
        private val responses = ArrayDeque(responses.toList())
        val sentBatches = mutableListOf<List<String>>()

        override fun send(
            envelopes: List<TelemetryEnvelope>,
            callback: (TelemetryBatchSendResult) -> Unit,
        ) {
            sentBatches.add(envelopes.map(TelemetryEnvelope::eventId))
            callback(responses.removeFirst())
        }
    }

    private class InMemoryQueue : TelemetryOfflineQueue {
        private val events = linkedMapOf<String, QueuedTelemetryEvent>()

        override fun enqueue(
            envelope: TelemetryEnvelope,
            queuedAt: Instant,
        ) {
            events[envelope.eventId] = QueuedTelemetryEvent(envelope, queuedAt)
        }

        override fun pending(
            limit: Int,
            now: Instant,
        ): List<QueuedTelemetryEvent> {
            return events.values
                .sortedWith(compareBy({ it.envelope.originalCollectedAt() }, { it.envelope.eventId }))
                .take(limit)
        }

        override fun acknowledge(eventIds: Set<String>) {
            eventIds.forEach(events::remove)
        }

        override fun recordFailure(
            eventIds: Set<String>,
            attemptedAt: Instant,
            error: String,
        ) {
            eventIds.forEach { eventId ->
                events.computeIfPresent(eventId) { _, queued ->
                    queued.copy(
                        attemptCount = queued.attemptCount + 1,
                        lastAttemptAt = attemptedAt,
                        lastError = error,
                    )
                }
            }
        }

        override fun size(now: Instant): Int = events.size
    }
}
