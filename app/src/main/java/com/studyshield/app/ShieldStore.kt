
package com.studyshield.app

import android.content.Context

object ShieldStore {
    private const val PREF = "study_shield"
    private const val ACTIVE = "active"
    private const val END_AT = "end_at"
    private const val PIN = "pin"
    private const val STREAK = "streak"
    private const val LAST_DAY = "last_day"
    private const val THEME = "theme"
    private const val PAUSED = "paused"
    private const val REMAINING = "remaining"

    private fun p(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun active(c: Context) = p(c).getBoolean(ACTIVE, false)
    fun setActive(c: Context, v: Boolean) = p(c).edit().putBoolean(ACTIVE, v).apply()

    fun endAt(c: Context) = p(c).getLong(END_AT, 0L)
    fun setEndAt(c: Context, v: Long) = p(c).edit().putLong(END_AT, v).apply()

    fun pin(c: Context) = p(c).getString(PIN, null)
    fun setPin(c: Context, v: String) = p(c).edit().putString(PIN, v).apply()

    fun streak(c: Context) = p(c).getInt(STREAK, 0)
    fun lastDay(c: Context) = p(c).getString(LAST_DAY, "")
    fun setStreak(c: Context, n: Int, day: String) =
        p(c).edit().putInt(STREAK, n).putString(LAST_DAY, day).apply()

    fun theme(c: Context) = p(c).getString(THEME, "Naruto") ?: "Naruto"
    fun setTheme(c: Context, v: String) = p(c).edit().putString(THEME, v).apply()

    fun paused(c: Context) = p(c).getBoolean(PAUSED, false)
    fun setPaused(c: Context, v: Boolean) = p(c).edit().putBoolean(PAUSED, v).apply()

    fun remaining(c: Context) = p(c).getLong(REMAINING, 45 * 60_000L)
    fun setRemaining(c: Context, v: Long) = p(c).edit().putLong(REMAINING, v).apply()

    fun blockedPackages(c: Context): Set<String> =
        p(c).getStringSet("blocked_packages", emptySet()) ?: emptySet()

    fun setBlockedPackages(c: Context, set: Set<String>) =
        p(c).edit().putStringSet("blocked_packages", set).apply()

    fun allowedMusicPackages(c: Context): Set<String> =
        p(c).getStringSet("music_packages", emptySet()) ?: emptySet()

    fun setAllowedMusicPackages(c: Context, set: Set<String>) =
        p(c).edit().putStringSet("music_packages", set).apply()

    fun educationChannels(c: Context): Set<String> =
        p(c).getStringSet("education_channels", emptySet()) ?: emptySet()

    fun setEducationChannels(c: Context, set: Set<String>) =
        p(c).edit().putStringSet("education_channels", set).apply()
}
