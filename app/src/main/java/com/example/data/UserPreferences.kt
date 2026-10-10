package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AIDifficulty

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dreamy_tac_toe_prefs", Context.MODE_PRIVATE)

    var playerName: String
        get() = prefs.getString("player_name", "Dreamer") ?: "Dreamer"
        set(value) = prefs.edit().putString("player_name", value).apply()

    var avatarId: String
        get() = prefs.getString("avatar_id", "cloud_bow") ?: "cloud_bow"
        set(value) = prefs.edit().putString("avatar_id", value).apply()

    var level: Int
        get() = prefs.getInt("player_level", 1)
        set(value) = prefs.edit().putInt("player_level", value).apply()

    var xp: Int
        get() = prefs.getInt("player_xp", 0)
        set(value) = prefs.edit().putInt("player_xp", value).apply()

    var hintsRemaining: Int
        get() = prefs.getInt("hints_count", 3)
        set(value) = prefs.edit().putInt("hints_count", value).apply()

    var undosRemaining: Int
        get() = prefs.getInt("undos_count", 3)
        set(value) = prefs.edit().putInt("undos_count", value).apply()

    var difficulty: AIDifficulty
        get() {
            val name = prefs.getString("ai_difficulty", AIDifficulty.MEDIUM.name) ?: AIDifficulty.MEDIUM.name
            return try {
                AIDifficulty.valueOf(name)
            } catch (_: Exception) {
                AIDifficulty.MEDIUM
            }
        }
        set(value) = prefs.edit().putString("ai_difficulty", value.name).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean("music_enabled", true)
        set(value) = prefs.edit().putBoolean("music_enabled", value).apply()

    var isSfxEnabled: Boolean
        get() = prefs.getBoolean("sfx_enabled", true)
        set(value) = prefs.edit().putBoolean("sfx_enabled", value).apply()

    var isVibrateEnabled: Boolean
        get() = prefs.getBoolean("vibrate_enabled", true)
        set(value) = prefs.edit().putBoolean("vibrate_enabled", value).apply()

    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean("seen_onboarding", false)
        set(value) = prefs.edit().putBoolean("seen_onboarding", value).apply()

    var dailyWinsCount: Int
        get() = prefs.getInt("daily_wins", 0)
        set(value) = prefs.edit().putInt("daily_wins", value).apply()

    var dailyChallengeClaimed: Boolean
        get() = prefs.getBoolean("daily_claimed", false)
        set(value) = prefs.edit().putBoolean("daily_claimed", value).apply()

    var totalWins: Int
        get() = prefs.getInt("total_wins", 0)
        set(value) = prefs.edit().putInt("total_wins", value).apply()

    var totalGames: Int
        get() = prefs.getInt("total_games", 0)
        set(value) = prefs.edit().putInt("total_games", value).apply()

    fun getXpForNextLevel(currentLevel: Int): Int {
        return currentLevel * 100
    }

    /**
     * Adds XP and returns true if user leveled up.
     */
    fun addXp(amount: Int): Boolean {
        var currentXp = xp + amount
        var currentLevel = level
        var leveledUp = false

        var needed = getXpForNextLevel(currentLevel)
        while (currentXp >= needed) {
            currentXp -= needed
            currentLevel++
            leveledUp = true
            needed = getXpForNextLevel(currentLevel)
        }

        level = currentLevel
        xp = currentXp
        return leveledUp
    }
}
