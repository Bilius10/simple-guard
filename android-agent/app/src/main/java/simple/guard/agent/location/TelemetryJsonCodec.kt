package simple.guard.agent.location

import org.json.JSONObject
import java.math.BigDecimal
import java.time.Instant

object TelemetryJsonCodec {
    fun envelopeToJson(envelope: TelemetryEnvelope): JSONObject =
        JSONObject()
            .put("eventId", envelope.eventId)
            .put("location", envelope.location?.toJson() ?: JSONObject.NULL)
            .put("technical", envelope.technical.toJson())

    fun envelopeFromJson(
        json: JSONObject,
        locationStatus: LocationCollectionStatus,
    ): TelemetryEnvelope {
        return TelemetryEnvelope(
            eventId = json.getString("eventId"),
            location = if (json.isNull("location")) null else json.getJSONObject("location").toLocation(),
            technical = json.getJSONObject("technical").toTechnical(),
            locationStatus = locationStatus,
        )
    }

    private fun LocationReading.toJson(): JSONObject =
        JSONObject()
            .put("latitude", latitude)
            .put("longitude", longitude)
            .putNullable("accuracyMeters", accuracyMeters)
            .putNullable("altitudeMeters", altitudeMeters)
            .putNullable("speedMetersPerSecond", speedMetersPerSecond)
            .put("provider", provider)
            .put("collectedAt", collectedAt.toString())

    private fun TechnicalTelemetryReading.toJson(): JSONObject =
        JSONObject()
            .putNullable("batteryLevelPercentage", batteryLevelPercentage)
            .putNullable("batteryCharging", batteryCharging)
            .putNullable("networkType", networkType?.name)
            .putNullable("signalStrengthDbm", signalStrengthDbm)
            .put("permissions", permissions?.toJson() ?: JSONObject.NULL)
            .put("collectedAt", collectedAt.toString())

    private fun TelemetryPermissions.toJson(): JSONObject =
        JSONObject()
            .putNullable("fineLocation", fineLocation?.name)
            .putNullable("coarseLocation", coarseLocation?.name)

    private fun JSONObject.toLocation(): LocationReading =
        LocationReading(
            latitude = BigDecimal(get("latitude").toString()),
            longitude = BigDecimal(get("longitude").toString()),
            accuracyMeters = decimalOrNull("accuracyMeters"),
            altitudeMeters = decimalOrNull("altitudeMeters"),
            speedMetersPerSecond = decimalOrNull("speedMetersPerSecond"),
            provider = getString("provider"),
            collectedAt = Instant.parse(getString("collectedAt")),
        )

    private fun JSONObject.toTechnical(): TechnicalTelemetryReading =
        TechnicalTelemetryReading(
            batteryLevelPercentage = intOrNull("batteryLevelPercentage"),
            batteryCharging = booleanOrNull("batteryCharging"),
            networkType = stringOrNull("networkType")?.let(NetworkType::valueOf),
            signalStrengthDbm = intOrNull("signalStrengthDbm"),
            permissions = if (isNull("permissions")) null else getJSONObject("permissions").toPermissions(),
            collectedAt = Instant.parse(getString("collectedAt")),
        )

    private fun JSONObject.toPermissions(): TelemetryPermissions =
        TelemetryPermissions(
            fineLocation = stringOrNull("fineLocation")?.let(PermissionState::valueOf),
            coarseLocation = stringOrNull("coarseLocation")?.let(PermissionState::valueOf),
        )

    private fun JSONObject.decimalOrNull(key: String): BigDecimal? {
        return if (isNull(key)) null else BigDecimal(get(key).toString())
    }

    private fun JSONObject.intOrNull(key: String): Int? {
        return if (isNull(key)) null else getInt(key)
    }

    private fun JSONObject.booleanOrNull(key: String): Boolean? {
        return if (isNull(key)) null else getBoolean(key)
    }

    private fun JSONObject.stringOrNull(key: String): String? {
        return if (isNull(key)) null else getString(key)
    }

    private fun JSONObject.putNullable(
        key: String,
        value: Any?,
    ): JSONObject {
        return put(key, value ?: JSONObject.NULL)
    }
}
