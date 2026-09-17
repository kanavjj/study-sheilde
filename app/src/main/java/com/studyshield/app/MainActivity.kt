
package com.studyshield.app

import android.app.*
import android.content.*
import android.graphics.Color
import android.os.*
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private lateinit var timerText: TextView
    private lateinit var statusText: TextView
    private lateinit var streakText: TextView
    private var handler = Handler(Looper.getMainLooper())
    private var totalMs = 45 * 60_000L

    private val tick = object : Runnable {
        override fun run() {
            if (ShieldStore.active(this@MainActivity) && !ShieldStore.paused(this@MainActivity)) {
                val end = ShieldStore.endAt(this@MainActivity)
                val left = (end - System.currentTimeMillis()).coerceAtLeast(0)
                ShieldStore.setRemaining(this@MainActivity, left)
                if (left == 0L) finishSession()
            }
            renderTimer()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8,11,24)
        window.navigationBarColor = Color.rgb(8,11,24)
        showHome()
        if (intent.getBooleanExtra("blocked_attempt", false)) {
            Toast.makeText(this, "🛡️ Blocked during Study Mode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
        if (ShieldStore.active(this) && !ShieldStore.paused(this)) showSession()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun tv(text: String, size: Float, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

    private fun button(text: String, onClick: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            setTextColor(Color.rgb(8,11,24))
            setBackgroundColor(Color.rgb(255,200,87))
            setOnClickListener { onClick() }
        }

    private fun base(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(18), dp(18), dp(24))
        setBackgroundColor(Color.rgb(8,11,24))
    }

    private fun showHome() {
        root = base()
        val scroll = ScrollView(this)
        val content = base()
        content.setPadding(dp(4), dp(8), dp(4), dp(24))
        val icon = ImageView(this).apply {
            setImageResource(com.studyshield.app.R.drawable.ic_shield)
            layoutParams = LinearLayout.LayoutParams(dp(96), dp(96)).apply { gravity = Gravity.CENTER }
        }
        content.addView(icon)
        content.addView(tv("STUDY SHIELD", 30f, true).apply { gravity = Gravity.CENTER })
        content.addView(tv("Your phone. Your focus. Your rules.", 15f).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        })
        streakText = tv("🔥 ${ShieldStore.streak(this)} day streak", 16f, true)
        content.addView(streakText.apply { gravity = Gravity.CENTER; setPadding(0,dp(16),0,dp(16)) })

        timerText = tv("45:00", 64f, true).apply { gravity = Gravity.CENTER }
        content.addView(timerText)
        statusText = tv("Ready for a focus session", 14f).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        }
        content.addView(statusText)

        content.addView(button("START STUDY MODE") { startSession() }.apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(0,dp(18),0,dp(10)) }
        })
        content.addView(button("⏱ Adjust Timer") { timerDialog() }.apply { setBackgroundColor(Color.rgb(27,35,64)); setTextColor(Color.WHITE) })
        content.addView(button("📝 Daily Planner & To‑Do") { plannerDialog() }.apply { setBackgroundColor(Color.rgb(27,35,64)); setTextColor(Color.WHITE) })
        content.addView(button("🎨 Themes") { themeDialog() }.apply { setBackgroundColor(Color.rgb(27,35,64)); setTextColor(Color.WHITE) })
        content.addView(button("🛡️ Blocking & Permissions") { setupDialog() }.apply { setBackgroundColor(Color.rgb(27,35,64)); setTextColor(Color.WHITE) })
        content.addView(tv("Naruto-inspired default theme • Music apps can be allowed • Education channels can be configured", 12f).apply {
            setTextColor(Color.GRAY); setPadding(dp(8),dp(20),dp(8),0)
        })

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
        renderTimer()
    }

    private fun showSession() {
        root = base()
        root.addView(tv("🛡️ STUDY MODE", 26f, true).apply { gravity = Gravity.CENTER })
        root.addView(tv("Distractions are blocked.", 14f).apply {
            gravity = Gravity.CENTER; setTextColor(Color.LTGRAY)
        })
        timerText = tv("45:00", 76f, true).apply { gravity = Gravity.CENTER; setPadding(0,dp(70),0,dp(20)) }
        root.addView(timerText)
        statusText = tv("FOCUS", 16f, true).apply { gravity = Gravity.CENTER }
        root.addView(statusText)

        val pause = button("⏸ PAUSE") { togglePause() }
        pause.layoutParams = LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(0,dp(28),0,dp(10)) }
        root.addView(pause)

        val restart = button("↻ RESTART") { restartSession() }
        restart.setBackgroundColor(Color.rgb(27,35,64)); restart.setTextColor(Color.WHITE)
        root.addView(restart)

        val end = button("🔐 END SESSION") { pinExit() }
        end.setBackgroundColor(Color.rgb(80,30,35)); end.setTextColor(Color.WHITE)
        root.addView(end)

        setContentView(root)
        renderTimer()
    }

    private fun renderTimer() {
        if (!::timerText.isInitialized) return
        val left = if (ShieldStore.active(this) && !ShieldStore.paused(this))
            (ShieldStore.endAt(this) - System.currentTimeMillis()).coerceAtLeast(0)
        else ShieldStore.remaining(this)
        val sec = left / 1000
        timerText.text = "%02d:%02d".format(sec / 60, sec % 60)
        if (::statusText.isInitialized) statusText.text =
            if (ShieldStore.paused(this)) "PAUSED" else if (ShieldStore.active(this)) "FOCUS" else "Ready"
    }

    private fun startSession() {
        if (ShieldStore.pin(this) == null) setPinDialog()
        val now = System.currentTimeMillis()
        ShieldStore.setRemaining(this, totalMs)
        ShieldStore.setEndAt(this, now + totalMs)
        ShieldStore.setPaused(this, false)
        ShieldStore.setActive(this, true)
        updateStreak()
        showSession()
    }

    private fun togglePause() {
        if (!ShieldStore.active(this)) return
        if (ShieldStore.paused(this)) {
            ShieldStore.setEndAt(this, System.currentTimeMillis() + ShieldStore.remaining(this))
            ShieldStore.setPaused(this, false)
        } else {
            ShieldStore.setRemaining(this, (ShieldStore.endAt(this) - System.currentTimeMillis()).coerceAtLeast(0))
            ShieldStore.setPaused(this, true)
        }
        showSession()
    }

    private fun restartSession() {
        ShieldStore.setRemaining(this, totalMs)
        ShieldStore.setEndAt(this, System.currentTimeMillis() + totalMs)
        ShieldStore.setPaused(this, false)
        ShieldStore.setActive(this, true)
        showSession()
    }

    private fun finishSession() {
        ShieldStore.setActive(this, false)
        ShieldStore.setPaused(this, false)
        Toast.makeText(this, "🎉 Session complete! Streak updated.", Toast.LENGTH_LONG).show()
        showHome()
    }

    private fun updateStreak() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val last = ShieldStore.lastDay(this)
        if (last == today) return
        val n = if (last.isBlank()) 1 else {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            if (last == yesterday) ShieldStore.streak(this) + 1 else 1
        }
        ShieldStore.setStreak(this, n, today)
    }

    private fun setPinDialog() {
        val input = EditText(this).apply { inputType = 2; hint = "4–8 digit PIN" }
        AlertDialog.Builder(this).setTitle("Create your Study Shield PIN")
            .setMessage("You need this PIN to end a study session early.")
            .setView(input).setPositiveButton("Save") { _, _ ->
                if (input.text.length in 4..8) ShieldStore.setPin(this, input.text.toString())
                else Toast.makeText(this, "PIN must be 4–8 digits.", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun pinExit() {
        val input = EditText(this).apply { inputType = 2; hint = "PIN" }
        AlertDialog.Builder(this).setTitle("🔐 Exit Study Mode")
            .setView(input).setPositiveButton("Exit") { _, _ ->
                if (input.text.toString() == ShieldStore.pin(this)) {
                    ShieldStore.setActive(this, false); ShieldStore.setPaused(this, false); showHome()
                } else Toast.makeText(this, "Wrong PIN.", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun timerDialog() {
        val input = EditText(this).apply { inputType = 2; hint = "Minutes (1–720)" }
        AlertDialog.Builder(this).setTitle("⏱ Adjust Study Timer")
            .setMessage("Choose any duration from 1 minute to 12 hours.")
            .setView(input).setPositiveButton("Set") { _, _ ->
                val m = input.text.toString().toLongOrNull()
                if (m != null && m in 1..720) {
                    totalMs = m * 60_000L
                    if (!ShieldStore.active(this)) ShieldStore.setRemaining(this, totalMs)
                    renderTimer()
                } else Toast.makeText(this, "Enter 1–720 minutes.", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun plannerDialog() {
        val box = EditText(this).apply {
            minLines = 10
            hint = "TODAY\n\n☐ 1.\n☐ 2.\n☐ 3.\n\nTop priority:\n\nNotes:"
            gravity = Gravity.TOP
        }
        AlertDialog.Builder(this).setTitle("📝 Undated Daily Planner + To‑Do")
            .setMessage("Your plan is saved only on this device in this starter build.")
            .setView(box).setPositiveButton("Save") { _, _ ->
                getPreferences(0).edit().putString("planner", box.text.toString()).apply()
            }.setNegativeButton("Cancel", null).show()
        box.setText(getPreferences(0).getString("planner", "") ?: "")
    }

    private fun themeDialog() {
        val themes = arrayOf("Naruto", "Sage Green", "Midnight", "Ocean", "Minimal")
        AlertDialog.Builder(this).setTitle("🎨 Choose Theme")
            .setSingleChoiceItems(themes, themes.indexOf(ShieldStore.theme(this))) { d, which ->
                ShieldStore.setTheme(this, themes[which])
                d.dismiss()
                Toast.makeText(this, "${themes[which]} selected. Full theme pack can be expanded.", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun setupDialog() {
        val msg = """
            1. Turn on Study Shield in Android Accessibility settings.
            2. Add distracting app package names to the block list.
            3. Add music apps to the allowed music list.
            4. Add education channel names for the controlled YouTube experience.

            The Android YouTube app cannot be reliably filtered to arbitrary channel names by a third-party app. This build therefore treats normal YouTube as blockable; an in-app controlled education-video experience should be used for channel-level allowlisting.
        """.trimIndent()
        AlertDialog.Builder(this).setTitle("🛡️ Shield Setup")
            .setMessage(msg)
            .setPositiveButton("Accessibility Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }.setNeutralButton("App Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }.setNegativeButton("Close", null).show()
    }
}
