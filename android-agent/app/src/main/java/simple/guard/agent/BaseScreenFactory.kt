package simple.guard.agent

import android.app.Activity
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import simple.guard.agent.welcome.WelcomeSummaryItem

internal open class BaseScreenFactory(
    protected val activity: Activity,
) {
    protected fun screenRoot(): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            minimumHeight = activity.resources.displayMetrics.heightPixels
            setBackgroundColor(AgentScreenTheme.SCREEN_BACKGROUND)
        }

    protected fun contentRoot(paddingTop: Int = 24): ContentRoot {
        val view =
            LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(22), dp(paddingTop), dp(22), dp(10))
            }
        return ContentRoot(
            view = view,
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f,
                ),
        )
    }

    protected fun scrollView(root: LinearLayout): ScrollView =
        ScrollView(activity).apply {
            setBackgroundColor(AgentScreenTheme.SCREEN_BACKGROUND)
            isFillViewport = true
            layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            addView(
                root,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        }

    protected fun header(): TextView =
        TextView(activity).apply {
            text = "SimpleGuard Agent"
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 13f
            includeFontPadding = false
            gravity = Gravity.CENTER
            setTextColor(AgentScreenTheme.ACCENT)
            setBackgroundColor(AgentScreenTheme.HEADER_BACKGROUND)
            setPadding(0, dp(18), 0, dp(18))
        }

    protected fun panel(title: String): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background =
                AgentScreenTheme.bordered(
                    AgentScreenTheme.PANEL_BACKGROUND,
                    AgentScreenTheme.BORDER,
                    dp(1),
                    dp(2),
                )
            setPadding(dp(14), dp(14), dp(14), dp(14))
            addView(panelTitle(title), bottomMargin(dp(10)))
        }

    protected fun panelTitle(text: String): TextView =
        TextView(activity).apply {
            this.text = text
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 15f
            includeFontPadding = false
            setTextColor(AgentScreenTheme.TEXT)
        }

    protected fun welcomeStatusBadge(value: String): TextView =
        TextView(activity).apply {
            text = "\u25CF  $value"
            typeface = Typeface.MONOSPACE
            textSize = 10f
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(AgentScreenTheme.LABEL)
            background =
                AgentScreenTheme.bordered(
                    0xFF263544.toInt(),
                    0xFF71808E.toInt(),
                    dp(1),
                    dp(2),
                )
            setPadding(dp(8), dp(5), dp(8), dp(5))
        }

    protected fun welcomeSummaryRow(item: WelcomeSummaryItem): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(36)
            background =
                AgentScreenTheme.bordered(
                    AgentScreenTheme.ROW_BACKGROUND,
                    AgentScreenTheme.ROW_BORDER,
                    dp(1),
                    dp(1),
                )
            setPadding(dp(9), dp(4), dp(9), dp(4))
            addView(
                technicalText(
                    item.label,
                    AgentScreenTheme.LABEL,
                    Gravity.START or Gravity.CENTER_VERTICAL,
                ),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f),
            )
            addView(
                technicalText(
                    item.value,
                    if (item.warning) AgentScreenTheme.WARNING else AgentScreenTheme.TEXT,
                    Gravity.END or Gravity.CENTER_VERTICAL,
                ),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.4f),
            )
        }

    protected fun numberedCapabilityRow(
        index: Int,
        value: String,
    ): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(36)
            background =
                AgentScreenTheme.bordered(
                    AgentScreenTheme.ROW_BACKGROUND,
                    AgentScreenTheme.ROW_BORDER,
                    dp(1),
                    dp(1),
                )
            setPadding(dp(9), dp(4), dp(9), dp(4))
            addView(
                technicalText(
                    index.toString(),
                    AgentScreenTheme.LABEL,
                    Gravity.START or Gravity.CENTER_VERTICAL,
                ),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.35f),
            )
            addView(
                technicalText(
                    value,
                    AgentScreenTheme.TEXT,
                    Gravity.END or Gravity.CENTER_VERTICAL,
                ),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.65f),
            )
        }

    protected fun technicalText(
        value: String,
        color: Int,
        textGravity: Int,
    ): TextView =
        TextView(activity).apply {
            text = value
            typeface = Typeface.MONOSPACE
            textSize = 10f
            includeFontPadding = false
            gravity = textGravity
            setTextColor(color)
            setPadding(0, 0, 0, 0)
        }

    protected fun badge(text: String): TextView =
        TextView(activity).apply {
            this.text = text
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 10f
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(5), dp(8), dp(5))
            background =
                AgentScreenTheme.bordered(
                    AgentScreenTheme.BADGE_BACKGROUND,
                    AgentScreenTheme.ACCENT,
                    dp(1),
                    dp(1),
                )
            setTextColor(AgentScreenTheme.ACCENT)
        }

    protected fun editableRow(
        label: String,
        hint: String,
    ): EditText {
        val container = rowContainer()
        container.addView(rowLabel(label))
        return EditText(activity).apply {
            this.hint = hint
            this.contentDescription = label
            typeface = Typeface.MONOSPACE
            textSize = 10f
            includeFontPadding = false
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setHintTextColor(AgentScreenTheme.MUTED)
            setTextColor(AgentScreenTheme.TEXT)
            setSingleLine(true)
            setPadding(dp(6), 0, 0, 0)
            background = null
            imeOptions = EditorInfo.IME_ACTION_NEXT
            container.addView(
                this,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.3f),
            )
        }
    }

    protected fun valueRow(
        label: String,
        value: String,
        valueColor: Int,
    ): TextView {
        val container = rowContainer()
        container.addView(rowLabel(label))
        return TextView(activity).apply {
            text = value
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 10f
            includeFontPadding = false
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setTextColor(valueColor)
            container.addView(
                this,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.3f),
            )
        }
    }

    protected fun rowContainer(): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(30)
            background =
                AgentScreenTheme.bordered(
                    AgentScreenTheme.ROW_BACKGROUND,
                    AgentScreenTheme.ROW_BORDER,
                    dp(1),
                    dp(1),
                )
            setPadding(dp(9), 0, dp(9), 0)
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(30),
                )
        }

    protected fun rowLabel(text: String): TextView =
        TextView(activity).apply {
            this.text = text
            typeface = Typeface.MONOSPACE
            textSize = 10f
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(AgentScreenTheme.LABEL)
            setPadding(0, 0, dp(8), 0)
            layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f,
                )
        }

    protected fun commandButton(text: String): Button =
        Button(activity).apply {
            this.text = text
            typeface = Typeface.MONOSPACE
            textSize = 11f
            isAllCaps = false
            includeFontPadding = false
            setTextColor(AgentScreenTheme.TEXT)
            background =
                AgentScreenTheme.bordered(
                    AgentScreenTheme.BUTTON_BACKGROUND,
                    AgentScreenTheme.ACCENT,
                    dp(1),
                    dp(2),
                )
            setPadding(0, dp(10), 0, dp(10))
        }

    protected fun footer(text: String): TextView =
        TextView(activity).apply {
            this.text = text
            typeface = Typeface.MONOSPACE
            textSize = 9f
            includeFontPadding = false
            setTextColor(AgentScreenTheme.MUTED)
            setBackgroundColor(AgentScreenTheme.HEADER_BACKGROUND)
            setPadding(dp(22), dp(8), dp(22), dp(8))
        }

    protected fun spacer(): View =
        View(activity).apply {
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f,
                )
        }

    protected fun bottomMargin(bottomMargin: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(0, 0, 0, bottomMargin)
        }

    protected fun wrapBottomMargin(bottomMargin: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(0, 0, 0, bottomMargin)
        }

    protected fun dp(value: Int): Int = AgentScreenTheme.dp(activity, value)

    protected data class ContentRoot(
        val view: LinearLayout,
        val layoutParams: LinearLayout.LayoutParams,
    )
}
