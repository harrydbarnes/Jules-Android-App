package com.jules.loader

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NewTabFragment : Fragment() {

    interface OnRepoSelectedListener {
        fun onRepoSelected(url: String, name: String)
    }

    private var listener: OnRepoSelectedListener? = null

    companion object {
        fun newInstance() = NewTabFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRepoSelectedListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<Button>(R.id.btn_load_home).setOnClickListener {
            listener?.onRepoSelected("https://jules.google.com", "Jules")
        }

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_recent_repos)
        recycler.layoutManager = LinearLayoutManager(context)
        val repos = RepoManager.getRecentRepos(requireContext())
        recycler.adapter = RecentReposAdapter(repos) { repo ->
            listener?.onRepoSelected(repo, repo.substringAfterLast("/")) 
        }
    }

    class RecentReposAdapter(private val repos: List<String>, private val onClick: (String) -> Unit) : RecyclerView.Adapter<RecentReposAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(R.id.text_repo_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_repo, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val repo = repos[position]
            holder.text.text = repo
            holder.itemView.setOnClickListener { onClick(repo) }
        }

        override fun getItemCount() = repos.size
    }
}
