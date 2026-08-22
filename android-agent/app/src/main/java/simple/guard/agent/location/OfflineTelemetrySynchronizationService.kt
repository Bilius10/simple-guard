package simple.guard.agent.location

import java.time.Instant

class OfflineTelemetrySynchronizationService(
    private val collector: LocationCollector,
    private val technicalCollector: TechnicalTelemetryCollector,
    private val sender: TelemetryBatchSender,
    private val queue: TelemetryOfflineQueue,
    private val eventIdProvider: () -> String,
    private val nowProvider: () -> Instant = Instant::now
) {

    fun synchronize(callback: (LocationSynchronizationResult) -> Unit) {
        val technical = technicalCollector.collect()
        collector.collect { collection ->
            val (location, locationStatus) = when (collection) {
                is LocationCollectionResult.Collected -> collection.reading to LocationCollectionStatus.COLLECTED
                LocationCollectionResult.PermissionDenied -> null to LocationCollectionStatus.PERMISSION_DENIED
                LocationCollectionResult.ProviderUnavailable -> null to LocationCollectionStatus.PROVIDER_UNAVAILABLE
                LocationCollectionResult.LocationUnavailable -> null to LocationCollectionStatus.LOCATION_UNAVAILABLE
            }
            queue.enqueue(
                TelemetryEnvelope(eventIdProvider(), location, technical, locationStatus),
                nowProvider()
            )
            drain(locationStatus, callback)
        }
    }

    fun retryPending(callback: (LocationSynchronizationResult) -> Unit) {
        drain(null, callback)
    }

    private fun drain(
        locationStatus: LocationCollectionStatus?,
        callback: (LocationSynchronizationResult) -> Unit
    ) {
        val now = nowProvider()
        val batch = queue.pending(BATCH_SIZE, now)
        if (batch.isEmpty()) {
            callback(LocationSynchronizationResult.Sent(locationStatus, 0))
            return
        }

        sender.send(batch.map(QueuedTelemetryEvent::envelope)) { sendResult ->
            when (sendResult) {
                is TelemetryBatchSendResult.Failed -> {
                    val eventIds = batch.mapTo(mutableSetOf()) { it.envelope.eventId }
                    queue.recordFailure(eventIds, nowProvider(), sendResult.error)
                    callback(LocationSynchronizationResult.SendFailure(locationStatus, queue.size(nowProvider())))
                }
                is TelemetryBatchSendResult.Completed -> {
                    processBatchResult(batch, sendResult.results, locationStatus, callback)
                }
            }
        }
    }

    private fun processBatchResult(
        batch: List<QueuedTelemetryEvent>,
        results: List<TelemetryBatchItemResult>,
        locationStatus: LocationCollectionStatus?,
        callback: (LocationSynchronizationResult) -> Unit
    ) {
        val resultsByEventId = results
            .filter { it.eventId != null }
            .associateBy { requireNotNull(it.eventId) }
        val acknowledged = mutableSetOf<String>()
        val retryable = mutableSetOf<String>()
        var retryError = "A instancia nao confirmou todos os eventos."

        batch.forEach { queued ->
            val eventId = queued.envelope.eventId
            val item = resultsByEventId[eventId]
            when (item?.status) {
                TelemetryBatchItemStatus.ACCEPTED,
                TelemetryBatchItemStatus.DUPLICATE,
                TelemetryBatchItemStatus.INVALID -> acknowledged.add(eventId)
                TelemetryBatchItemStatus.UNAUTHORIZED,
                TelemetryBatchItemStatus.FAILED,
                null -> {
                    retryable.add(eventId)
                    retryError = item?.error ?: retryError
                }
            }
        }

        queue.acknowledge(acknowledged)
        if (retryable.isNotEmpty()) {
            queue.recordFailure(retryable, nowProvider(), retryError)
            callback(LocationSynchronizationResult.SendFailure(locationStatus, queue.size(nowProvider())))
            return
        }
        drain(locationStatus, callback)
    }

    private companion object {
        const val BATCH_SIZE = 100
    }
}
