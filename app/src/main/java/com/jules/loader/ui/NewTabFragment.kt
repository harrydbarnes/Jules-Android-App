package com.jules.loader.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jules.loader.R
import com.jules.loader.data.JulesRepository
import com.jules.loader.data.model.SourceContext
import kotlinx.coroutines.launch

class NewTabFragment : Fragment() {

    private lateinit var repository: JulesRepository
    private lateinit var adapter: SourcesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = JulesRepository.getInstance(requireContext().applicationContext)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_recent_repos)
        val emptyState = view.findViewById<TextView>(R.id.text_empty_state)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = SourcesAdapter()
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.btn_load_home).setOnClickListener {
            // Load home action (e.g., navigate to WebView or similar)
            android.widget.Toast.makeText(context, "Loading Jules Home...", android.widget.Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            try {
                val sources = repository.getSources()
                if (sources.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE
                    adapter.submitList(sources)
                } else {
                    recyclerView.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // Handle error
                android.util.Log.e("NewTabFragment", "Error loading sources", e)
            }
        }
    }

    class SourcesAdapter : ListAdapter<SourceContext, SourcesAdapter.SourceViewHolder>(SourceDiffCallback()) {

        class SourceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val repoName: TextView = view.findViewById(R.id.text_repo_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_repo, parent, false)
            return SourceViewHolder(view)
        }

        override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
            val source = getItem(position)
            // Extract repo name if it's a URL or just show raw string
            val displayText = if (source.source.contains("github.com/")) {
                source.source.substringAfter("github.com/")
            } else {
                source.source
            }
            holder.repoName.text = displayText
        }

        class SourceDiffCallback : DiffUtil.ItemCallback<SourceContext>() {
            override fun areItemsTheSame(oldItem: SourceContext, newItem: SourceContext): Boolean {
                return oldItem.source == newItem.source
            }

            override fun areContentsTheSame(oldItem: SourceContext, newItem: SourceContext): Boolean {
                return oldItem == newItem
            }
        }
    }
}
