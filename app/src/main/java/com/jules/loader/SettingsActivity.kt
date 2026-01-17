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
        val prefs = getSharedPreferences("jules_prefs", Context.MODE_PRIVATE)

        switch.isChecked = prefs.getBoolean("allow_unlimited_tabs", false)

        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("allow_unlimited_tabs", isChecked).apply()
        }
    }
}
