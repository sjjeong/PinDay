package com.dino.pinday.data.preferences

import com.dino.pinday.data.db.PinDayDatabase

class AppPreferences(private val database: PinDayDatabase) {

    private val queries get() = database.settingsQueries

    fun isOnboardingComplete(): Boolean {
        return queries.selectByKey(KEY_ONBOARDING_COMPLETE)
            .executeAsOneOrNull() == "true"
    }

    fun setOnboardingComplete() {
        queries.insertOrReplace(KEY_ONBOARDING_COMPLETE, "true")
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
