package com.concept60.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.concept60.app.data.model.*
import com.concept60.app.data.repository.ConceptRepository
import com.concept60.app.data.repository.SavedConceptsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ConceptUiState {
    object Idle : ConceptUiState()
    object Loading : ConceptUiState()
    data class Success(val result: ConceptResult) : ConceptUiState()
    data class Error(val message: String) : ConceptUiState()
}

sealed class VideoUiState {
    object Idle : VideoUiState()
    object Loading : VideoUiState()
    data class Success(val video: VideoResponse) : VideoUiState()
    data class Error(val message: String) : VideoUiState()
}

@HiltViewModel
class ConceptViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val savedRepo: SavedConceptsRepository,
) : ViewModel() {

    private val _conceptState = MutableStateFlow<ConceptUiState>(ConceptUiState.Idle)
    val conceptState: StateFlow<ConceptUiState> = _conceptState.asStateFlow()

    private val _videoState = MutableStateFlow<VideoUiState>(VideoUiState.Idle)
    val videoState: StateFlow<VideoUiState> = _videoState.asStateFlow()

    private val _lessonSaved = MutableStateFlow(false)
    val lessonSaved: StateFlow<Boolean> = _lessonSaved.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    val learningProgress = savedRepo.learningProgress

    fun loadConcept(concept: String, category: String) {
        viewModelScope.launch {
            _conceptState.value = ConceptUiState.Loading
            conceptRepository.getConcept(concept, category).fold(
                onSuccess = { result ->
                    _conceptState.value = ConceptUiState.Success(result)
                    // Save to recent searches
                    savedRepo.addRecentSearch(
                        SavedConcept(
                            id = "${concept.lowercase()}-${category.lowercase()}",
                            concept = concept,
                            category = category,
                            oneLiner = result.oneLiner,
                            scenario = result.scenario,
                        )
                    )
                    savedRepo.incrementConceptsReviewed()
                },
                onFailure = {
                    _conceptState.value = ConceptUiState.Error(
                        it.message ?: "Unable to load the concept explanation."
                    )
                }
            )
        }

        viewModelScope.launch {
            _videoState.value = VideoUiState.Loading
            conceptRepository.getVideo(concept, category).fold(
                onSuccess = { _videoState.value = VideoUiState.Success(it) },
                onFailure = { _videoState.value = VideoUiState.Error(it.message ?: "Unable to generate video.") }
            )
        }
    }

    fun markQuizCompleted() {
        if (_quizCompleted.value) return
        _quizCompleted.value = true
        viewModelScope.launch { savedRepo.incrementQuizzesCompleted() }
    }

    fun saveLesson(concept: String, category: String, result: ConceptResult) {
        if (_lessonSaved.value) return
        _lessonSaved.value = true
        viewModelScope.launch {
            savedRepo.addRecentSearch(
                SavedConcept(
                    id = "${concept.lowercase()}-${category.lowercase()}",
                    concept = concept,
                    category = category,
                    oneLiner = result.oneLiner,
                    scenario = result.scenario,
                )
            )
        }
    }
}
