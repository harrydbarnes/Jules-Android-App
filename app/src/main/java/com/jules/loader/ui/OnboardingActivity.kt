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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.jules.loader.MainActivity
import com.jules.loader.R
import com.jules.loader.data.JulesRepository
import com.jules.loader.databinding.ActivityOnboardingBinding

import android.os.Handler
import android.os.Looper

class OnboardingActivity : AppCompatActivity() {

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
            SlideData("Multi-tab browsing", "Open multiple sessions and switch between them easily."),
            SlideData("Quick repository access", "Access your recent repositories with a single tap."),
            SlideData("", "") // Placeholder for API slide
        )

        override fun getItemViewType(position: Int): Int {
            return if (position == slides.size - 1) 1 else 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 1) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_api, parent, false)
                ApiViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_page, parent, false)
                SlideViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is SlideViewHolder) {
                holder.bind(slides[position])
            } else if (holder is ApiViewHolder) {
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

        inner class ApiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val input: TextInputEditText = itemView.findViewById(R.id.apiKeyInput)
            private val getKeyLink: TextView = itemView.findViewById(R.id.getKeyLink)
            private val saveButton: Button = itemView.findViewById(R.id.saveButton)

            fun bind() {
                if (detectedApiKey != null) {
                    input.setText(detectedApiKey)
                }

                getKeyLink.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jules.google.com/settings#api"))
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
