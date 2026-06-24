package com.concept60.app.data.model

import com.google.gson.annotations.SerializedName

// ── API request/response models ──────────────────────────────────────────────

data class ConceptRequest(
    val concept: String,
    val category: String,
)

data class ConceptResponse(
    val concept: String = "",
    val oneLiner: String = "",
    val scenario: String = "",
    val exampleScenarios: List<String> = emptyList(),
    val keywords: List<Any> = emptyList(), // can be String or {term, definition}
    val quiz: List<QuizQuestionModel>? = null
)

data class QuizQuestionModel(
    val question: String,
    val options: List<String>,
    val answer: String,
    val level: String = "Medium" // Beginner, Medium, Hard
)

data class VideoResponse(
    val type: String = "",
    val videoUrl: String = "",
    val summary: String = "",
)

data class PdfQuestionRequest(
    val pdfText: String,
    val question: String,
)

data class PdfAnswerResponse(
    val answer: String = "",
)

// ── Parsed keyword with optional definition ───────────────────────────────────

data class Keyword(
    val term: String,
    val definition: String = "",
)

// ── Normalized result used in the UI ─────────────────────────────────────────

data class ConceptResult(
    val concept: String,
    val oneLiner: String,
    val scenario: String,
    val exampleScenarios: List<String>,
    val keywords: List<Keyword>,
    val quiz: List<QuizQuestionModel> = emptyList()
)

// ── Saved search / history item ───────────────────────────────────────────────

data class SavedConcept(
    val id: String = "",
    val concept: String = "",
    val category: String = "General",
    val oneLiner: String = "",
    val scenario: String = "",
    val exampleScenarios: List<String> = emptyList(),
    val keywords: List<Keyword> = emptyList(),
    val searchedAt: Long = System.currentTimeMillis(),
)

// ── Learning progress ─────────────────────────────────────────────────────────

data class LearningProgress(
    val conceptsReviewed: Int = 0,
    val quizzesCompleted: Int = 0,
    val pdfQuestionsAnswered: Int = 0,
)

// ── Accessibility / reading settings ─────────────────────────────────────────

enum class FontSize { SMALL, MEDIUM, LARGE }
enum class ReadingMode { NORMAL, FOCUS, DYSLEXIA }
enum class AppTheme { DARK, LIGHT }

data class AccessibilitySettings(
    val fontSize: FontSize = FontSize.MEDIUM,
    val readingMode: ReadingMode = ReadingMode.NORMAL,
    val playbackSpeed: Float = 1.0f,
    val listenMode: Boolean = false,
    val theme: AppTheme = AppTheme.DARK,
)

// ── Auth ──────────────────────────────────────────────────────────────────────

data class UserProfile(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)

// ── Trending concept ──────────────────────────────────────────────────────────

data class TrendingConcept(
    val name: String,
    val searches: Int,
    val change: String,
    val category: String = "General",
)
