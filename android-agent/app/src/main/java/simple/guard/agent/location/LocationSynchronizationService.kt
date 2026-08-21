package simple.guard.agent.location

class LocationSynchronizationService(
    private val collector: LocationCollector,
    private val technicalCollector: TechnicalTelemetryCollector,
    private val sender: LocationSender,
    private val eventIdProvider: () -> String
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
            send(
                TelemetryEnvelope(eventIdProvider(), location, technical, locationStatus),
                callback
            )
        }
    }

    private fun send(
        envelope: TelemetryEnvelope,
        callback: (LocationSynchronizationResult) -> Unit
    ) {
        sender.send(envelope) { result ->
            callback(when (result) {
                TelemetrySendResult.Sent -> LocationSynchronizationResult.Sent(envelope.locationStatus)
                TelemetrySendResult.Failed -> LocationSynchronizationResult.SendFailure(envelope.locationStatus)
            })
        }
    }
}
