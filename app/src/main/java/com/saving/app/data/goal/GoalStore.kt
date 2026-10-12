package com.saving.app.data.goal

import android.content.Context

/**
 * Savings goal (name + target amount) is stored locally per device via SharedPreferences —
 * deliberately NOT synced through Drive like transactions/categories are. If you want it to
 * sync across both devices too, say so and I can add that.
 */
class GoalStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getGoalName(): String? = prefs.getString(KEY_NAME, null)

    fun getGoalAmount(): Double? {
        val raw = prefs.getFloat(KEY_AMOUNT, -1f)
        return if (raw < 0f) null else raw.toDouble()
    }

    fun setGoal(name: String, amount: Double) {
        prefs.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_AMOUNT, amount.toFloat())
            .apply()
    }

    fun clearGoal() {
        prefs.edit().remove(KEY_NAME).remove(KEY_AMOUNT).apply()
    }

    companion object {
        private const val PREFS_NAME = "savings_goal_prefs"
        private const val KEY_NAME = "goal_name"
        private const val KEY_AMOUNT = "goal_amount"
    }
}
