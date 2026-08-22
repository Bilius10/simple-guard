package simple.guard.agent.location

import java.io.File
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileTelemetryOfflineQueueTests {

    @Test
    fun persistsAndOrdersEventsByOriginalCollectionTimeTests() {
        withQueueFile { file ->
            val queuedAt = Instant.parse("2026-08-20T12:00:00Z")
            val queue = FileTelemetryOfflineQueue(file)
            queue.enqueue(envelope("later", queuedAt.plusSeconds(60)), queuedAt)
            queue.enqueue(envelope("earlier", queuedAt.minusSeconds(60)), queuedAt.plusSeconds(1))

            val reloaded = FileTelemetryOfflineQueue(file)
            assertEquals(listOf("earlier", "later"), reloaded.pending(100, queuedAt).map { it.envelope.eventId })

            reloaded.acknowledge(setOf("earlier"))
            reloaded.recordFailure(setOf("later"), queuedAt.plusSeconds(2), "offline")

            val pending = reloaded.pending(100, queuedAt.plusSeconds(2)).single()
            assertEquals(1, pending.attemptCount)
            assertEquals(queuedAt.plusSeconds(2), pending.lastAttemptAt)
            assertEquals("offline", pending.lastError)
            assertEquals(1, reloaded.size(queuedAt.plusSeconds(2)))

            reloaded.acknowledge(setOf("later"))
            assertEquals(0, reloaded.size(queuedAt.plusSeconds(2)))
        }
    }

    @Test
    fun appliesSevenDayRetentionAndOldestFirstCapacityTests() {
        withQueueFile { file ->
            val now = Instant.parse("2026-08-20T12:00:00Z")
            val queue = FileTelemetryOfflineQueue(file, Duration.ofDays(7), maxEvents = 2)
            queue.enqueue(envelope("expired", now.minus(Duration.ofDays(8))), now.minus(Duration.ofDays(8)))
            queue.enqueue(envelope("first", now.minusSeconds(3)), now)
            queue.enqueue(envelope("second", now.minusSeconds(2)), now.plusSeconds(1))
            queue.enqueue(envelope("third", now.minusSeconds(1)), now.plusSeconds(2))

            assertEquals(listOf("second", "third"), queue.pending(100, now.plusSeconds(2)).map { it.envelope.eventId })
            assertEquals(7L, FileTelemetryOfflineQueue.RETENTION_DAYS)
            assertEquals(1_000, FileTelemetryOfflineQueue.MAX_EVENTS)
        }
    }

    @Test
    fun roundTripsNullableTelemetryFieldsTests() {
        withQueueFile { file ->
            val now = Instant.parse("2026-08-20T12:00:00Z")
            val envelope = TelemetryEnvelope(
                eventId = "nullable",
                location = null,
                technical = TechnicalTelemetryReading(null, null, null, null, null, now),
                locationStatus = LocationCollectionStatus.PERMISSION_DENIED
            )
            val queue = FileTelemetryOfflineQueue(file)
            queue.enqueue(envelope, now)

            val persisted = FileTelemetryOfflineQueue(file).pending(1, now).single()
            assertEquals(envelope, persisted.envelope)
            assertNull(persisted.lastAttemptAt)
            assertNull(persisted.lastError)
        }
    }

    private fun envelope(eventId: String, collectedAt: Instant): TelemetryEnvelope {
        return TelemetryEnvelope(
            eventId = eventId,
            location = LocationReading(
                latitude = BigDecimal("-23.55052"),
                longitude = BigDecimal("-46.633308"),
                accuracyMeters = BigDecimal("4.5"),
                altitudeMeters = null,
                speedMetersPerSecond = null,
                provider = "GPS",
                collectedAt = collectedAt
            ),
            technical = TechnicalTelemetryReading(
                batteryLevelPercentage = 70,
                batteryCharging = false,
                networkType = NetworkType.WIFI,
                signalStrengthDbm = -55,
                permissions = TelemetryPermissions(PermissionState.GRANTED, PermissionState.DENIED),
                collectedAt = collectedAt.plusSeconds(2)
            ),
            locationStatus = LocationCollectionStatus.COLLECTED
        )
    }

    private fun withQueueFile(block: (File) -> Unit) {
        val directory = createTempDirectory("simpleguard-queue-tests").toFile()
        try {
            block(File(directory, "queue.json"))
        } finally {
            directory.deleteRecursively()
        }
    }
}
