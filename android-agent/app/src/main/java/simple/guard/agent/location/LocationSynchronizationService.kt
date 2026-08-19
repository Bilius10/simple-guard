package simple.guard.agent.location

class LocationSynchronizationService(
    private val collector: LocationCollector,
    private val sender: LocationSender
) {

    fun synchronize(callback: (LocationSynchronizationResult) -> Unit) {
        collector.collect { collection ->
            when (collection) {
                is LocationCollectionResult.Collected -> send(collection.reading, callback)
                LocationCollectionResult.PermissionDenied -> callback(LocationSynchronizationResult.PermissionDenied)
                LocationCollectionResult.ProviderUnavailable -> callback(LocationSynchronizationResult.ProviderUnavailable)
                LocationCollectionResult.LocationUnavailable -> callback(LocationSynchronizationResult.LocationUnavailable)
            }
        }
    }

    private fun send(
        reading: LocationReading,
        callback: (LocationSynchronizationResult) -> Unit
    ) {
        sender.send(reading) { result ->
            callback(when (result) {
                LocationSendResult.Sent -> LocationSynchronizationResult.Sent
                LocationSendResult.Failed -> LocationSynchronizationResult.SendFailure
            })
        }
    }
}
