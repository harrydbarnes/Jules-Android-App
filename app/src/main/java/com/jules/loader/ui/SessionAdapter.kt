package com.jules.loader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.jules.loader.R
import com.jules.loader.data.model.Session

import android.content.Intent
import android.app.Activity
import androidx.core.app.ActivityOptionsCompat

class SessionAdapter : ListAdapter<Session, SessionAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.sessionTitle)
        private val prompt: TextView = itemView.findViewById(R.id.sessionPrompt)
        private val sourceChip: Chip = itemView.findViewById(R.id.sourceChip)
        private val statusChip: Chip = itemView.findViewById(R.id.statusChip)
        private val pulseView: View = itemView.findViewById(R.id.pulseView)

        fun bind(session: Session) {
            val context = itemView.context
            title.text = session.title ?: context.getString(R.string.untitled_session)
            prompt.text = session.prompt ?: context.getString(R.string.no_prompt)

            val status = session.status ?: "Idle"
            statusChip.text = status

            // Set status color
            when(status) {
                "PR Open" -> statusChip.setChipBackgroundColorResource(R.color.status_pr_open)
                "Executing Tests" -> statusChip.setChipBackgroundColorResource(R.color.status_tests_passing)
                else -> statusChip.setChipBackgroundColorResource(R.color.jules_purple_light)
            }

            // Simple pulse animation (alpha)
            val animation = android.view.animation.AlphaAnimation(0.4f, 1.0f)
            animation.duration = if (status == "Idle") 0 else 1000 // Only pulse if active
            animation.repeatCount = if (status == "Idle") 0 else android.view.animation.Animation.INFINITE
            animation.repeatMode = android.view.animation.Animation.REVERSE
            pulseView.startAnimation(animation)

            val source = session.sourceContext?.source ?: context.getString(R.string.unknown_source)
            // Clean up source string (e.g. "sources/github/user/repo" -> "user/repo")
            val cleanSource = if (source.startsWith("sources/github/")) {
                source.removePrefix("sources/github/")
            } else {
                source
            }
            sourceChip.text = cleanSource

            itemView.transitionName = "shared_element_container_${session.id}"

            itemView.setOnClickListener {
                val intent = Intent(context, TaskDetailActivity::class.java).apply {
                    putExtra(TaskDetailActivity.EXTRA_SESSION_ID, session.id)
                    putExtra(TaskDetailActivity.EXTRA_SESSION_TITLE, session.title)
                    putExtra(TaskDetailActivity.EXTRA_SESSION_PROMPT, session.prompt)
                    putExtra(TaskDetailActivity.EXTRA_SESSION_STATUS, session.status)
                    putExtra(TaskDetailActivity.EXTRA_SESSION_SOURCE, session.sourceContext?.source)
                    putExtra(TaskDetailActivity.EXTRA_SESSION_BRANCH, session.sourceContext?.githubRepoContext?.startingBranch)
                }

                val activity = context as? Activity
                if (activity != null) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        itemView,
                        "shared_element_container_${session.id}"
                    )
                    context.startActivity(intent, options.toBundle())
                } else {
                    context.startActivity(intent)
                }
            }
        }
    }

    class SessionDiffCallback : DiffUtil.ItemCallback<Session>() {
        override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem == newItem
        }
    }
}
