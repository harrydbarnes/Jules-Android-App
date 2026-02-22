package com.jules.loader

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors

import com.google.android.material.color.DynamicColorsOptions
import com.jules.loader.util.ThemeUtils

class JulesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { activity, _ ->
                    ThemeUtils.getSelectedTheme(activity) == ThemeUtils.THEME_CUTTLEFISH
                }
                .build()
        )
    }
}
