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

        val payload = TelemetrySignaturePayload.bytes(
            "00000000-0000-0000-0000-000000000901",
            "android-agent-location",
            TelemetryEnvelope(
                eventId = "00000000-0000-0000-0000-000000000903",
                location = reading,
                technical = TechnicalTelemetryReading(
                    batteryLevelPercentage = 12,
                    batteryCharging = false,
                    networkType = NetworkType.CELLULAR,
                    signalStrengthDbm = -101,
                    permissions = TelemetryPermissions(PermissionState.GRANTED, PermissionState.GRANTED),
                    collectedAt = Instant.parse("2026-08-17T12:00:02Z")
                ),
                locationStatus = LocationCollectionStatus.COLLECTED
            )
        ).toString(Charsets.UTF_8)

        assertEquals(
            "INGEST_TELEMETRY\n" +
                "00000000-0000-0000-0000-000000000901\n" +
                "android-agent-location\n" +
                "00000000-0000-0000-0000-000000000903\n" +
                "1\n2026-08-17T12:00:00Z\n" +
                "-23.55052\n-46.633308\n4.5\n\n0\nGPS\n" +
                "1\n2026-08-17T12:00:02Z\n12\nfalse\nCELLULAR\n-101\nGRANTED\nGRANTED",
            payload
        )
    }

    @Test
    fun writesEmptyFieldsForMissingOptionalMetricsTests() {
        val payload = TelemetrySignaturePayload.bytes(
            "00000000-0000-0000-0000-000000000902",
            "android-agent-location",
            TelemetryEnvelope(
                eventId = "00000000-0000-0000-0000-000000000904",
                location = null,
                technical = TechnicalTelemetryReading(
                    batteryLevelPercentage = null,
                    batteryCharging = null,
                    networkType = null,
                    signalStrengthDbm = null,
                    permissions = null,
                    collectedAt = Instant.parse("2026-08-17T12:05:00Z")
                ),
                locationStatus = LocationCollectionStatus.LOCATION_UNAVAILABLE
            )
        ).toString(Charsets.UTF_8)

        assertEquals(
            "INGEST_TELEMETRY\n" +
                "00000000-0000-0000-0000-000000000902\n" +
                "android-agent-location\n" +
                "00000000-0000-0000-0000-000000000904\n" +
                "0\n\n\n\n\n\n\n\n" +
                "1\n2026-08-17T12:05:00Z\n\n\n\n\n\n",
            payload
        )
    }
}
