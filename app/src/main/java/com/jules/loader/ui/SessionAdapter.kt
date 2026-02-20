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

        fun bind(session: Session) {
            val context = itemView.context
            title.text = session.title ?: context.getString(R.string.untitled_session)
            prompt.text = session.prompt ?: context.getString(R.string.no_prompt)

            val source = session.sourceContext?.source ?: context.getString(R.string.unknown_source)
            // Clean up source string (e.g. "sources/github/user/repo" -> "user/repo")
            val cleanSource = if (source.startsWith("sources/github/")) {
                source.removePrefix("sources/github/")
            } else {
                source
            }
            sourceChip.text = cleanSource
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
