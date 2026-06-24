package com.concept60.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.concept60.app.data.model.AccessibilitySettings
import com.concept60.app.data.model.LearningProgress
import com.concept60.app.data.model.SavedConcept
import com.concept60.app.data.model.TrendingConcept
import com.concept60.app.data.repository.ConceptRepository
import com.concept60.app.data.repository.SavedConceptsRepository
import com.concept60.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── SavedViewModel ────────────────────────────────────────────────────────────

sealed class SavedUiState {
    object Loading : SavedUiState()
    data class Success(val items: List<SavedConcept>) : SavedUiState()
    data class Error(val message: String) : SavedUiState()
}

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val savedRepo: SavedConceptsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<SavedUiState>(SavedUiState.Loading)
    val state: StateFlow<SavedUiState> = _state.asStateFlow()
    
    val learningProgress: StateFlow<LearningProgress> = savedRepo.learningProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearningProgress())

    val dayStreak: StateFlow<Int> = _state.map { state ->
        if (state is SavedUiState.Success) {
            calculateStreak(state.items)
        } else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            // Start from local data immediately
            savedRepo.recentSearches.collect { local ->
                _state.value = SavedUiState.Success(local)
            }
        }
        loadCloud()
    }

    private fun calculateStreak(items: List<SavedConcept>): Int {
        if (items.isEmpty()) return 0
        // Use UTC days for streak calculation to avoid timezone issues during travel
        val dayMillis = 24 * 60 * 60 * 1000L
        val sortedDays = items.map { it.searchedAt / dayMillis }.distinct().sortedDescending()
        
        val today = System.currentTimeMillis() / dayMillis
        val yesterday = today - 1
        
        if (sortedDays.isEmpty()) return 0
        // If the latest activity isn't today or yesterday, streak is broken
        if (sortedDays[0] < yesterday) return 0
        
        var streak = 1
        var current = sortedDays[0]
        for (i in 1 until sortedDays.size) {
            if (sortedDays[i] == current - 1) {
                streak++
                current = sortedDays[i]
            } else {
                break
            }
        }
        return streak
    }

    private fun loadCloud() {
        viewModelScope.launch {
            savedRepo.fetchCloudSaved().onSuccess { cloudItems ->
                if (cloudItems.isNotEmpty()) {
                    _state.value = SavedUiState.Success(cloudItems)
                }
            }
        }
    }

    fun delete(item: SavedConcept) {
        viewModelScope.launch {
            if (item.id.isNotEmpty()) {
                savedRepo.deleteCloudSaved(item.id)
            }
            savedRepo.removeRecentSearch(item.id)
        }
    }
}

// ── TrendingViewModel ─────────────────────────────────────────────────────────

sealed class TrendingUiState {
    object Loading : TrendingUiState()
    data class Success(val items: List<TrendingConcept>) : TrendingUiState()
    data class Error(val message: String) : TrendingUiState()
}

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val conceptRepo: ConceptRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<TrendingUiState>(TrendingUiState.Loading)
    val state: StateFlow<TrendingUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = TrendingUiState.Loading
            conceptRepo.getTrending().fold(
                onSuccess = { _state.value = TrendingUiState.Success(it) },
                onFailure = { _state.value = TrendingUiState.Error(it.message ?: "Failed to load trending data") }
            )
        }
    }
}

// ── SettingsViewModel ─────────────────────────────────────────────────────────

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AccessibilitySettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccessibilitySettings())

    fun update(settings: AccessibilitySettings) {
        viewModelScope.launch { settingsRepo.updateSettings(settings) }
    }
}
