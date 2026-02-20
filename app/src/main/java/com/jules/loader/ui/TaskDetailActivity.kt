package com.jules.loader.ui

import android.content.Context
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

class TaskDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        val title = intent.getStringExtra("EXTRA_SESSION_TITLE")
        findViewById<TextView>(R.id.detailTitle).text = title ?: "Task Details"

        // Setup Log Bottom Sheet
        val bottomSheet = findViewById<View>(R.id.logBottomSheet)
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Setup Log RecyclerView
        val logRecycler = findViewById<RecyclerView>(R.id.logRecyclerView)
        logRecycler.layoutManager = LinearLayoutManager(this)
        logRecycler.adapter = LogAdapter(generateFakeLogs())
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
            holder.text.setTextColor(holder.itemView.context.getColor(android.R.color.white))
        }

        override fun getItemCount() = logs.size
    }
}
