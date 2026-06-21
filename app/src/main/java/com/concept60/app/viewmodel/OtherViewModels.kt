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

    init {
        viewModelScope.launch {
            // Start from local data immediately
            savedRepo.recentSearches.collect { local ->
                _state.value = SavedUiState.Success(local)
            }
        }
        loadCloud()
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
