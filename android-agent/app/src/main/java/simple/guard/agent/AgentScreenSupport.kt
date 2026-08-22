package simple.guard.agent

import android.app.Activity
import android.graphics.drawable.GradientDrawable

internal data class LocalPairing(
    val deviceId: String,
    val deviceName: String,
    val instanceUrl: String,
    val pendingSynchronization: Boolean,
)

internal object AgentScreenTheme {
    const val SCREEN_BACKGROUND = 0xFF001021.toInt()
    const val HEADER_BACKGROUND = 0xFF000911.toInt()
    const val PANEL_BACKGROUND = 0xFF061923.toInt()
    const val ROW_BACKGROUND = 0xFF071B24.toInt()
    const val BADGE_BACKGROUND = 0xFF062237.toInt()
    const val BUTTON_BACKGROUND = 0xFF053B55.toInt()
    const val BORDER = 0xFF10B8D8.toInt()
    const val ROW_BORDER = 0xFF104554.toInt()
    const val ACCENT = 0xFF3EDCF4.toInt()
    const val TEXT = 0xFFE8FBFF.toInt()
    const val LABEL = 0xFF8CB0BC.toInt()
    const val MUTED = 0xFF6C8791.toInt()
    const val WARNING = 0xFFFFD84D.toInt()
    const val SUCCESS = 0xFF1AFFA9.toInt()
    const val DANGER = 0xFFFF5B5B.toInt()

    fun dp(
        activity: Activity,
        value: Int,
    ): Int {
        return (value * activity.resources.displayMetrics.density).toInt()
    }

    fun bordered(
        backgroundColor: Int,
        strokeColor: Int,
        strokeWidth: Int,
        radius: Int,
    ): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(backgroundColor)
            setStroke(strokeWidth, strokeColor)
        }
}
