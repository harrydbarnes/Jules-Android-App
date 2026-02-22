package com.jules.loader.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jules.loader.util.ThemeUtils

open class BaseActivity : AppCompatActivity() {

    private var currentTheme: String? = null
    private var isDynamic: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must apply theme before super.onCreate()
        currentTheme = ThemeUtils.getSelectedTheme(this)
        isDynamic = ThemeUtils.isDynamicColorsEnabled(this)
        ThemeUtils.applyTheme(this) // Applies base theme (Octopus/Squid) only. Dynamic Colors handled in Application.

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        // Check if theme preference has changed while activity was paused
        val newTheme = ThemeUtils.getSelectedTheme(this)
        val newIsDynamic = ThemeUtils.isDynamicColorsEnabled(this)

        if (currentTheme != newTheme || isDynamic != newIsDynamic) {
            recreate()
        }
    }
}
