package com.jules.loader

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors

class JulesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("jules_settings", Context.MODE_PRIVATE)
        if (prefs.getBoolean("use_dynamic_colours", true)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}
