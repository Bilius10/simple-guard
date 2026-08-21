package simple.guard.agent.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TechnicalTelemetryValueNormalizerTests {

    @Test
    fun preservesValidAndEmptyBatteryLevelsTests() {
        assertEquals(67, TechnicalTelemetryValueNormalizer.batteryPercentage(67, 100))
        assertEquals(0, TechnicalTelemetryValueNormalizer.batteryPercentage(0, 100))
        assertNull(TechnicalTelemetryValueNormalizer.batteryPercentage(-1, 100))
        assertNull(TechnicalTelemetryValueNormalizer.batteryPercentage(50, 0))
    }

    @Test
    fun preservesLowBatteryLevelTests() {
        assertEquals(5, TechnicalTelemetryValueNormalizer.batteryPercentage(5, 100))
    }

    @Test
    fun rejectsUnavailableOrInvalidSignalReadingsTests() {
        assertEquals(-95, TechnicalTelemetryValueNormalizer.signalStrengthDbm(-95))
        assertEquals(-160, TechnicalTelemetryValueNormalizer.signalStrengthDbm(-160))
        assertEquals(0, TechnicalTelemetryValueNormalizer.signalStrengthDbm(0))
        assertNull(TechnicalTelemetryValueNormalizer.signalStrengthDbm(null))
        assertNull(TechnicalTelemetryValueNormalizer.signalStrengthDbm(-127))
        assertNull(TechnicalTelemetryValueNormalizer.signalStrengthDbm(-161))
        assertNull(TechnicalTelemetryValueNormalizer.signalStrengthDbm(1))
    }
}
