package com.jules.loader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jules.loader.R
import com.jules.loader.data.JulesRepository
import com.jules.loader.data.model.SourceContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateTaskViewModel(private val repository: JulesRepository) : ViewModel() {

    private val _availableSources = MutableStateFlow<List<SourceContext>>(emptyList())
    val availableSources: StateFlow<List<SourceContext>> = _availableSources

    private val _errorEvent = MutableSharedFlow<Int>()
    val errorEvent: SharedFlow<Int> = _errorEvent

    private val _taskCreatedEvent = MutableSharedFlow<Unit>()
    val taskCreatedEvent: SharedFlow<Unit> = _taskCreatedEvent

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    companion object {
        private val TAG = CreateTaskViewModel::class.java.simpleName
    }

    init {
        loadSources()
    }

    private fun loadSources() {
        viewModelScope.launch {
            try {
                val sources = repository.getSources()
                _availableSources.value = sources
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to load repositories", e)
                _errorEvent.emit(R.string.error_load_repositories)
            }
        }
    }

    fun submitTask(prompt: String, repoUrl: String?, branch: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.createSession(prompt, repoUrl, branch)
                _taskCreatedEvent.emit(Unit)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error creating session", e)
                _errorEvent.emit(R.string.error_create_task)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
