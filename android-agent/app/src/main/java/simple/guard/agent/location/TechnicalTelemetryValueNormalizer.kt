package simple.guard.agent.location

object TechnicalTelemetryValueNormalizer {
    fun batteryPercentage(
        level: Int,
        scale: Int,
    ): Int? {
        if (level < 0) return null
        if (scale <= 0) return null
        return (level * 100 / scale).coerceIn(0, 100)
    }

    fun signalStrengthDbm(value: Int?): Int? {
        if (value == null) return null
        if (value == INVALID_WIFI_RSSI) return null
        if (value < MIN_SIGNAL_DBM) return null
        if (value > MAX_SIGNAL_DBM) return null
        return value
    }

    private const val INVALID_WIFI_RSSI = -127
    private const val MIN_SIGNAL_DBM = -160
    private const val MAX_SIGNAL_DBM = 0
}
