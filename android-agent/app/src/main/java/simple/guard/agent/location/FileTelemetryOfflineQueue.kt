package simple.guard.agent.location

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.time.Instant

class FileTelemetryOfflineQueue(
    private val storageFile: File,
    private val retention: Duration = Duration.ofDays(RETENTION_DAYS),
    private val maxEvents: Int = MAX_EVENTS
) : TelemetryOfflineQueue {

    @Synchronized
    override fun enqueue(envelope: TelemetryEnvelope, queuedAt: Instant) {
        val events = pruned(load(), queuedAt)
            .filterNot { it.envelope.eventId == envelope.eventId }
            .plus(QueuedTelemetryEvent(envelope, queuedAt))
            .sortedWith(eventComparator)
            .takeLast(maxEvents)
        persist(events)
    }

    @Synchronized
    override fun pending(limit: Int, now: Instant): List<QueuedTelemetryEvent> {
        require(limit > 0) { "limit must be positive" }
        val events = pruned(load(), now)
        persist(events)
        return events.sortedWith(eventComparator).take(limit)
    }

    @Synchronized
    override fun acknowledge(eventIds: Set<String>) {
        if (eventIds.isEmpty()) return
        persist(load().filterNot { it.envelope.eventId in eventIds })
    }

    @Synchronized
    override fun recordFailure(eventIds: Set<String>, attemptedAt: Instant, error: String) {
        if (eventIds.isEmpty()) return
        persist(load().map { queued ->
            if (queued.envelope.eventId !in eventIds) {
                queued
            } else {
                queued.copy(
                    attemptCount = queued.attemptCount + 1,
                    lastAttemptAt = attemptedAt,
                    lastError = error
                )
            }
        })
    }

    @Synchronized
    override fun size(now: Instant): Int {
        val events = pruned(load(), now)
        persist(events)
        return events.size
    }

    private fun pruned(events: List<QueuedTelemetryEvent>, now: Instant): List<QueuedTelemetryEvent> {
        val cutoff = now.minus(retention)
        return events
            .filter { !it.queuedAt.isBefore(cutoff) }
            .sortedWith(eventComparator)
            .takeLast(maxEvents)
    }

    private fun load(): List<QueuedTelemetryEvent> {
        if (!storageFile.exists() || storageFile.length() == 0L) return emptyList()
        return runCatching {
            val json = JSONObject(storageFile.readText(Charsets.UTF_8))
            val events = json.getJSONArray("events")
            List(events.length()) { index -> events.getJSONObject(index).toQueuedEvent() }
        }.getOrElse { emptyList() }
    }

    private fun persist(events: List<QueuedTelemetryEvent>) {
        val parent = requireNotNull(storageFile.absoluteFile.parentFile)
        parent.mkdirs()
        val temporary = File(parent, storageFile.name + ".tmp")
        val root = JSONObject()
            .put("version", STORAGE_VERSION)
            .put("events", JSONArray().apply {
                events.forEach { put(it.toJson()) }
            })
        FileOutputStream(temporary).use { output ->
            output.write(root.toString().toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        try {
            Files.move(
                temporary.toPath(),
                storageFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary.toPath(), storageFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun QueuedTelemetryEvent.toJson(): JSONObject = JSONObject()
        .put("envelope", TelemetryJsonCodec.envelopeToJson(envelope))
        .put("locationStatus", envelope.locationStatus.name)
        .put("queuedAt", queuedAt.toString())
        .put("attemptCount", attemptCount)
        .putNullable("lastAttemptAt", lastAttemptAt?.toString())
        .putNullable("lastError", lastError)

    private fun JSONObject.toQueuedEvent(): QueuedTelemetryEvent {
        val locationStatus = LocationCollectionStatus.valueOf(getString("locationStatus"))
        return QueuedTelemetryEvent(
            envelope = TelemetryJsonCodec.envelopeFromJson(getJSONObject("envelope"), locationStatus),
            queuedAt = Instant.parse(getString("queuedAt")),
            attemptCount = getInt("attemptCount"),
            lastAttemptAt = if (isNull("lastAttemptAt")) null else Instant.parse(getString("lastAttemptAt")),
            lastError = if (isNull("lastError")) null else getString("lastError")
        )
    }

    private fun JSONObject.putNullable(key: String, value: Any?): JSONObject {
        return put(key, value ?: JSONObject.NULL)
    }

    companion object {
        const val RETENTION_DAYS = 7L
        const val MAX_EVENTS = 1_000

        private const val STORAGE_VERSION = 1
        private val eventComparator = compareBy<QueuedTelemetryEvent>(
            { it.envelope.originalCollectedAt() },
            { it.queuedAt },
            { it.envelope.eventId }
        )
    }
}
