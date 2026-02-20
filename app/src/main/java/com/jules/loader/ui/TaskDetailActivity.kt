package com.jules.loader.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jules.loader.R
import com.jules.loader.databinding.ActivityTaskDetailBinding

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding

    companion object {
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_SESSION_TITLE = "EXTRA_SESSION_TITLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra(EXTRA_SESSION_TITLE)
        binding.detailTitle.text = title ?: "Task Details"

        // Setup Log Bottom Sheet
        val behavior = BottomSheetBehavior.from(binding.logBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Setup Log RecyclerView
        binding.logRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.logRecyclerView.adapter = LogAdapter(generateFakeLogs())
    }

    private fun generateFakeLogs(): List<String> {
        return List(20) { "Log entry #$it: Operation successful." }
    }

    class LogAdapter(private val logs: List<String>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {
        class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return LogViewHolder(view)
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            holder.text.text = logs[position]
            // Removed hardcoded text color
        }

        override fun getItemCount() = logs.size
    }
}
