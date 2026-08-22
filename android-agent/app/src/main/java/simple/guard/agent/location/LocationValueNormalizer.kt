package simple.guard.agent.location

import java.math.BigDecimal
import java.math.RoundingMode

object LocationValueNormalizer {
    fun latitude(value: Double): BigDecimal = decimal(value, 8)

    fun longitude(value: Double): BigDecimal = decimal(value, 8)

    fun accuracyMeters(value: Double): BigDecimal = decimal(value, 3)

    fun altitudeMeters(value: Double): BigDecimal = decimal(value, 3)

    fun speedMetersPerSecond(value: Double): BigDecimal = decimal(value, 3)

    private fun decimal(
        value: Double,
        scale: Int,
    ): BigDecimal {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP)
    }
}
