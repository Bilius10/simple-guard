package simple.guard.agent.location

import kotlin.test.Test
import kotlin.test.assertEquals

class LocationValueNormalizerTests {
    @Test
    fun normalizesLatitudeAndLongitudeToEightFractionDigitsTests() {
        assertEquals("-23.55052012", LocationValueNormalizer.latitude(-23.55052012345).toPlainString())
        assertEquals("-46.63330899", LocationValueNormalizer.longitude(-46.63330898765).toPlainString())
    }

    @Test
    fun normalizesTelemetryMetricsToThreeFractionDigitsTests() {
        assertEquals("4.568", LocationValueNormalizer.accuracyMeters(4.5678).toPlainString())
        assertEquals("760.124", LocationValueNormalizer.altitudeMeters(760.1236).toPlainString())
        assertEquals("0.346", LocationValueNormalizer.speedMetersPerSecond(0.3456).toPlainString())
    }
}
