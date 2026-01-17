package com.jules.loader

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switch = findViewById<SwitchMaterial>(R.id.switch_unlimited_tabs)
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        switch.isChecked = prefs.getBoolean(Constants.PREF_ALLOW_UNLIMITED_TABS, false)

        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(Constants.PREF_ALLOW_UNLIMITED_TABS, isChecked).apply()
        }
    }
}
