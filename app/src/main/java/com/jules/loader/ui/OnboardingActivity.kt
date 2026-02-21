package com.jules.loader.ui

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.jules.loader.MainActivity
import com.jules.loader.R
import com.jules.loader.data.JulesRepository
import com.jules.loader.databinding.ActivityOnboardingBinding
import com.jules.loader.util.ThemeUtils

class OnboardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var detectedApiKey: String? = null
    private val repository: JulesRepository by lazy { JulesRepository.getInstance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = OnboardingAdapter()
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        if (savedInstanceState != null) {
            val position = savedInstanceState.getInt("current_page", 0)
            binding.viewPager.setCurrentItem(position, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_page", binding.viewPager.currentItem)
    }

    override fun onResume() {
        super.onResume()
        checkClipboard()
    }

    private fun checkClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        try {
            if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                val item = clipboard.primaryClip?.getItemAt(0)
                val text = item?.text?.toString()

                // Heuristic for Jules API Key (Google API Key starts with AIza)
                if (!text.isNullOrEmpty() && text.startsWith("AIza") && text.length > 20) {
                     detectedApiKey = text
                     val lastPage = (binding.viewPager.adapter?.itemCount ?: 1) - 1
                     // Notify adapter to update view if visible, or it will be picked up on bind
                     binding.viewPager.adapter?.notifyItemChanged(lastPage)

                     // Scroll to last page if found
                     binding.viewPager.currentItem = lastPage
                }
            }
        } catch (e: Exception) {
            // Ignore clipboard errors
        }
    }

    private inner class OnboardingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val slides = listOf(
            SlideData(getString(R.string.onboarding_manage_sessions_title), getString(R.string.onboarding_manage_sessions_desc)),
            SlideData(getString(R.string.onboarding_live_logs_title), getString(R.string.onboarding_live_logs_desc)),
            SlideData(getString(R.string.onboarding_theme_title), getString(R.string.onboarding_theme_desc)),
            SlideData("", "") // Placeholder for API slide
        )

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                slides.size - 1 -> 1 // API
                slides.size - 2 -> 2 // Theme
                else -> 0
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                1 -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_api, parent, false)
                    ApiViewHolder(view)
                }
                2 -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_theme, parent, false)
                    ThemeViewHolder(view)
                }
                else -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_page, parent, false)
                    SlideViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is SlideViewHolder) {
                holder.bind(slides[position])
            } else if (holder is ApiViewHolder) {
                holder.bind()
            } else if (holder is ThemeViewHolder) {
                holder.bind()
            }
        }

        override fun getItemCount(): Int = slides.size

        inner class SlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val title: TextView = itemView.findViewById(R.id.title)
            private val desc: TextView = itemView.findViewById(R.id.description)

            fun bind(data: SlideData) {
                title.text = data.title
                desc.text = data.desc
            }
        }

        inner class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val radioGroup: RadioGroup = itemView.findViewById(R.id.themeRadioGroup)

            fun bind() {
                val currentTheme = ThemeUtils.getSelectedTheme(this@OnboardingActivity)
                if (currentTheme == ThemeUtils.THEME_SQUID) {
                    radioGroup.check(R.id.radioSquid)
                } else {
                    radioGroup.check(R.id.radioOctopus)
                }

                radioGroup.setOnCheckedChangeListener { _, checkedId ->
                    val newTheme = if (checkedId == R.id.radioSquid) ThemeUtils.THEME_SQUID else ThemeUtils.THEME_OCTOPUS
                    if (newTheme != ThemeUtils.getSelectedTheme(this@OnboardingActivity)) {
                        ThemeUtils.setSelectedTheme(this@OnboardingActivity, newTheme)
                        recreate()
                    }
                }
            }
        }

        inner class ApiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val input: TextInputEditText = itemView.findViewById(R.id.apiKeyInput)
            private val getKeyLink: TextView = itemView.findViewById(R.id.getKeyLink)
            private val saveButton: Button = itemView.findViewById(R.id.saveButton)

            fun bind() {
                if (detectedApiKey != null) {
                    input.setText(detectedApiKey)
                }

                getKeyLink.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jules.google.com/settings/api"))
                    startActivity(browserIntent)
                }

                saveButton.setOnClickListener {
                    val key = input.text?.toString()?.trim()
                    if (key.isNullOrEmpty()) {
                        Toast.makeText(this@OnboardingActivity, R.string.error_enter_api_key, Toast.LENGTH_SHORT).show()
                    } else {
                        repository.saveApiKey(key)
                        startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    data class SlideData(val title: String, val desc: String)
}
