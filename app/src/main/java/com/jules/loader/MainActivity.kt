package com.jules.loader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jules.loader.data.JulesRepository
import com.jules.loader.data.model.Session
import com.jules.loader.databinding.ActivityMainBinding
import com.jules.loader.ui.BaseActivity
import com.jules.loader.ui.OnboardingActivity
import com.jules.loader.ui.SessionAdapter
import com.jules.loader.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: JulesRepository
    private lateinit var adapter: SessionAdapter
    private var allSessions: List<Session> = emptyList()

    private var selectedRepo: String? = null
    private var selectedStatus: String? = null
    private var selectedDateRange: String? = null
    private var searchQuery: String = ""
    private var isFilterActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = JulesRepository.getInstance(applicationContext)

        if (repository.getApiKey().isNullOrEmpty()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.sessions_title)

        adapter = SessionAdapter()
        binding.sessionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.sessionsRecyclerView.adapter = adapter

        // Set default item animator to ensure add/remove animations are smooth
        binding.sessionsRecyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()

        binding.sessionsRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    binding.fab.shrink()
                } else if (dy < 0) {
                    binding.fab.extend()
                }
            }
        })

        binding.fab.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM) // Haptic
            val intent = Intent(this, com.jules.loader.ui.CreateTaskActivity::class.java)
            val options = android.app.ActivityOptions.makeSceneTransitionAnimation(
                this, binding.fab, "shared_element_container"
            )
            startActivity(intent, options.toBundle())
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadSessions(forceRefresh = true)
        }

        setupSearch()
        setupFilters()
        loadSessions()
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, AutoTransition())
            binding.filterContainer.visibility = View.GONE
            binding.searchContainer.visibility = View.VISIBLE
            binding.searchEditText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.btnSearchBack.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, AutoTransition())
            binding.searchContainer.visibility = View.GONE
            binding.filterContainer.visibility = View.VISIBLE
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
            binding.searchEditText.setText("") // Clear search on close
        }

        binding.btnSearchClear.setOnClickListener {
            binding.searchEditText.setText("")
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString()
                applyFilters()
            }
        })
    }

    private fun setupFilters() {
        val touchListener = View.OnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                when (v.id) {
                    R.id.chipRepo -> showRepoMenu(v)
                    R.id.chipStatus -> showStatusMenu(v)
                    R.id.chipDate -> showDateMenu(v)
                }
                v.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
            }
            true
        }

        binding.chipRepo.setOnTouchListener(touchListener)
        binding.chipStatus.setOnTouchListener(touchListener)
        binding.chipDate.setOnTouchListener(touchListener)
    }

    private fun showRepoMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        val repos = allSessions.mapNotNull { it.sourceContext?.source }
            .map { if (it.startsWith("sources/github/")) it.removePrefix("sources/github/") else it }
            .distinct()
            .sorted()

        popup.menu.add(0, 0, 0, getString(R.string.filter_all))
        repos.forEachIndexed { index, repo ->
            popup.menu.add(0, index + 1, index + 1, repo)
        }

        popup.setOnMenuItemClickListener { item ->
            selectedRepo = if (item.itemId == 0) null else repos[item.itemId - 1]
            binding.chipRepo.text = if (selectedRepo == null) getString(R.string.filter_repo) else item.title
            binding.chipRepo.isChecked = selectedRepo != null
            applyFilters()
            true
        }
        popup.show()
    }

    private fun showStatusMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        val statuses = allSessions.mapNotNull { it.status }.distinct().sorted()

        popup.menu.add(0, 0, 0, getString(R.string.filter_all))
        statuses.forEachIndexed { index, status ->
            popup.menu.add(0, index + 1, index + 1, status)
        }

        popup.setOnMenuItemClickListener { item ->
            selectedStatus = if (item.itemId == 0) null else statuses[item.itemId - 1]
            binding.chipStatus.text = selectedStatus ?: getString(R.string.filter_status)
            binding.chipStatus.isChecked = selectedStatus != null
            applyFilters()
            true
        }
        popup.show()
    }

    private fun showDateMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 0, 0, getString(R.string.filter_all))
        popup.menu.add(0, 1, 1, getString(R.string.date_today))
        popup.menu.add(0, 2, 2, getString(R.string.date_yesterday))
        popup.menu.add(0, 3, 3, getString(R.string.date_7_days))
        popup.menu.add(0, 4, 4, getString(R.string.date_30_days))

        popup.setOnMenuItemClickListener { item ->
            selectedDateRange = if (item.itemId == 0) null else item.title.toString()
            binding.chipDate.text = selectedDateRange ?: getString(R.string.filter_date)
            binding.chipDate.isChecked = selectedDateRange != null
            applyFilters()
            true
        }
        popup.show()
    }

    private fun applyFilters() {
        var filtered = allSessions

        // 1. Search Filter
        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase(Locale.getDefault())
            filtered = filtered.filter { session ->
                (session.title?.lowercase(Locale.getDefault())?.contains(query) == true) ||
                (session.prompt?.lowercase(Locale.getDefault())?.contains(query) == true)
            }
        }

        // 2. Repo Filter
        selectedRepo?.let { repo ->
             filtered = filtered.filter {
                 val source = it.sourceContext?.source
                 if (source != null) {
                     val cleanSource = if (source.startsWith("sources/github/")) source.removePrefix("sources/github/") else source
                     cleanSource == repo
                 } else {
                     false
                 }
             }
        }

        // 3. Status Filter
        selectedStatus?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        // 4. Date Filter
        selectedDateRange?.let { range ->
            val now = Calendar.getInstance()
            filtered = filtered.filter { session ->
                val date = DateUtils.parseDate(session.createTime)
                if (date != null) {
                    val sessionCal = Calendar.getInstance()
                    sessionCal.time = date

                    val diff = now.timeInMillis - sessionCal.timeInMillis
                    val daysDiff = diff / (1000 * 60 * 60 * 24)

                    when (range) {
                        getString(R.string.date_today) -> isSameDay(now, sessionCal)
                        getString(R.string.date_yesterday) -> isYesterday(now, sessionCal)
                        getString(R.string.date_7_days) -> daysDiff in 0..7
                        getString(R.string.date_30_days) -> daysDiff in 0..30
                        else -> true
                    }
                } else {
                    false
                }
            }
        }

        adapter.submitList(filtered)
        updateFilterIcon()
    }

    private fun updateFilterIcon() {
        val hasFilter = selectedRepo != null || selectedStatus != null || selectedDateRange != null
        if (hasFilter == isFilterActive) return

        isFilterActive = hasFilter
        val iconRes = if (hasFilter) R.drawable.ic_close else R.drawable.ic_filter_list
        val desc = if (hasFilter) getString(R.string.clear_filters) else getString(R.string.filters_label)

        val outAnim = ObjectAnimator.ofFloat(binding.btnFilter, "translationX", 0f, -binding.btnFilter.width.toFloat())
        outAnim.duration = 150
        outAnim.interpolator = AccelerateDecelerateInterpolator()
        outAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.btnFilter.setImageResource(iconRes)
                binding.btnFilter.contentDescription = desc
                if (hasFilter) {
                    binding.btnFilter.setOnClickListener { clearFilters() }
                    binding.btnFilter.isClickable = true
                } else {
                    binding.btnFilter.setOnClickListener(null)
                    binding.btnFilter.isClickable = false
                }

                val inAnim = ObjectAnimator.ofFloat(binding.btnFilter, "translationX", binding.btnFilter.width.toFloat(), 0f)
                inAnim.duration = 150
                inAnim.interpolator = AccelerateDecelerateInterpolator()
                inAnim.start()
            }
        })
        outAnim.start()
    }

    private fun clearFilters() {
        selectedRepo = null
        selectedStatus = null
        selectedDateRange = null

        binding.chipRepo.text = getString(R.string.filter_repo)
        binding.chipRepo.isChecked = false

        binding.chipStatus.text = getString(R.string.filter_status)
        binding.chipStatus.isChecked = false

        binding.chipDate.text = getString(R.string.filter_date)
        binding.chipDate.isChecked = false

        applyFilters()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, target: Calendar): Boolean {
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, target)
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, com.jules.loader.ui.SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, com.jules.loader.ui.AboutActivity::class.java))
                true
            }
            R.id.action_gift -> {
                val url = "https://jules.google/docs/changelog"
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(this, Uri.parse(url))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSessions(forceRefresh: Boolean = false) {
        val isFirstLoad = !forceRefresh && !repository.hasCachedSessions()
        if (isFirstLoad) {
            binding.skeletonLayout.visibility = View.VISIBLE
            binding.errorText.visibility = View.GONE
            binding.sessionsRecyclerView.visibility = View.GONE
        }

        lifecycleScope.launch {
            try {
                allSessions = repository.getSessions(forceRefresh)

                if (allSessions.isEmpty()) {
                    binding.errorText.text = getString(R.string.no_sessions)
                    binding.errorText.visibility = View.VISIBLE
                    adapter.submitList(emptyList())
                } else {
                    binding.errorText.visibility = View.GONE
                    binding.sessionsRecyclerView.visibility = View.VISIBLE
                    applyFilters()
                    if (isFirstLoad) {
                        binding.sessionsRecyclerView.scheduleLayoutAnimation()
                    }
                }
            } catch (e: java.io.IOException) {
                binding.errorText.text = getString(R.string.error_loading_sessions, e.localizedMessage)
                binding.errorText.visibility = View.VISIBLE
                android.util.Log.e("MainActivity", "Error loading sessions", e)
            } catch (e: retrofit2.HttpException) {
                binding.errorText.text = getString(R.string.error_loading_sessions, e.message())
                binding.errorText.visibility = View.VISIBLE
                android.util.Log.e("MainActivity", "Error loading sessions", e)
            } finally {
                binding.skeletonLayout.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
