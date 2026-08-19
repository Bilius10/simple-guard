package simple.guard.agent.location

import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationSignaturePayloadTests {

    @Test
    fun createsCanonicalLocationPayloadTests() {
        val reading = LocationReading(
            latitude = BigDecimal("-23.55052000"),
            longitude = BigDecimal("-46.63330800"),
            accuracyMeters = BigDecimal("4.500"),
            altitudeMeters = null,
            speedMetersPerSecond = BigDecimal("0.000"),
            provider = "GPS",
            collectedAt = Instant.parse("2026-08-17T12:00:00Z")
        )

        val payload = LocationSignaturePayload.bytes(
            "00000000-0000-0000-0000-000000000901",
            "android-agent-location",
            reading
        ).toString(Charsets.UTF_8)

        assertEquals(
            "INGEST_LOCATION\n" +
                "00000000-0000-0000-0000-000000000901\n" +
                "android-agent-location\n" +
                "2026-08-17T12:00:00Z\n" +
                "-23.55052\n-46.633308\n4.5\n\n0\nGPS",
            payload
        )
    }

    @Test
    fun writesEmptyFieldsForMissingOptionalMetricsTests() {
        val reading = LocationReading(
            latitude = BigDecimal("-23.55052000"),
            longitude = BigDecimal("-46.63330800"),
            accuracyMeters = null,
            altitudeMeters = null,
            speedMetersPerSecond = null,
            provider = "NETWORK",
            collectedAt = Instant.parse("2026-08-17T12:05:00Z")
        )

        val payload = LocationSignaturePayload.bytes(
            "00000000-0000-0000-0000-000000000902",
            "android-agent-location",
            reading
        ).toString(Charsets.UTF_8)

        assertEquals(
            "INGEST_LOCATION\n" +
                "00000000-0000-0000-0000-000000000902\n" +
                "android-agent-location\n" +
                "2026-08-17T12:05:00Z\n" +
                "-23.55052\n-46.633308\n\n\n\nNETWORK",
            payload
        )
    }
}
