package com.concept60.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.concept60.app.data.api.Concept60Api
import com.concept60.app.data.model.LearningProgress
import com.concept60.app.data.model.SavedConcept
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedConceptsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val api: Concept60Api,
) {
    private val gson = Gson()

    // ── DataStore keys ────────────────────────────────────────────────────────
    private val KEY_RECENT = stringPreferencesKey("recent_searches")
    private val KEY_CONCEPTS_REVIEWED = intPreferencesKey("concepts_reviewed")
    private val KEY_QUIZZES_COMPLETED = intPreferencesKey("quizzes_completed")
    private val KEY_PDF_ANSWERS = intPreferencesKey("pdf_answers")

    // ── Recent searches (local) ───────────────────────────────────────────────

    val recentSearches: Flow<List<SavedConcept>> = dataStore.data.map { prefs ->
        val json = prefs[KEY_RECENT] ?: return@map emptyList()
        val type = object : TypeToken<List<SavedConcept>>() {}.type
        try { gson.fromJson(json, type) } catch (e: Exception) { emptyList() }
    }

    suspend fun addRecentSearch(concept: SavedConcept) {
        dataStore.edit { prefs ->
            val type = object : TypeToken<List<SavedConcept>>() {}.type
            val current: MutableList<SavedConcept> = try {
                gson.fromJson(prefs[KEY_RECENT] ?: "[]", type)
            } catch (e: Exception) { mutableListOf() }

            // Deduplicate by concept name
            current.removeAll { it.concept.equals(concept.concept, ignoreCase = true) }
            current.add(0, concept)
            prefs[KEY_RECENT] = gson.toJson(current.take(50))
        }

        // We no longer save to Firestore here because the server /api/concept 
        // is now the single source of truth for Firestore writes.
    }

    suspend fun removeRecentSearch(id: String) {
        dataStore.edit { prefs ->
            val type = object : TypeToken<List<SavedConcept>>() {}.type
            val current: MutableList<SavedConcept> = try {
                gson.fromJson(prefs[KEY_RECENT] ?: "[]", type)
            } catch (e: Exception) { mutableListOf() }
            current.removeAll { it.id == id }
            prefs[KEY_RECENT] = gson.toJson(current)
        }
    }

    // ── Cloud saved searches via backend API ──────────────────────────────────

    suspend fun fetchCloudSaved(): Result<List<SavedConcept>> {
        return try {
            val raw = api.getSavedSearches()
            val mapped = raw.mapNotNull { map ->
                val concept = map["concept"]?.toString() ?: return@mapNotNull null
                
                // Parse date if available, otherwise use current
                val dateStr = map["createdAt"]?.toString() ?: map["searchedAt"]?.toString()
                val timestamp = try {
                    // Try to parse ISO date if backend sends it as string, or long
                    dateStr?.toLong() ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                SavedConcept(
                    id = map["id"]?.toString() ?: map["_id"]?.toString() ?: "",
                    concept = concept,
                    category = map["category"]?.toString() ?: "General",
                    oneLiner = map["oneLiner"]?.toString() ?: "",
                    scenario = map["scenario"]?.toString() ?: "",
                    searchedAt = timestamp
                )
            }
            Result.success(mapped.sortedByDescending { it.searchedAt })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCloudSaved(id: String): Result<Unit> {
        return try {
            api.deleteSavedSearch(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Learning progress ─────────────────────────────────────────────────────

    val learningProgress: Flow<LearningProgress> = dataStore.data.map { prefs ->
        LearningProgress(
            conceptsReviewed = prefs[KEY_CONCEPTS_REVIEWED] ?: 0,
            quizzesCompleted = prefs[KEY_QUIZZES_COMPLETED] ?: 0,
            pdfQuestionsAnswered = prefs[KEY_PDF_ANSWERS] ?: 0,
        )
    }

    suspend fun incrementConceptsReviewed() {
        dataStore.edit { prefs ->
            prefs[KEY_CONCEPTS_REVIEWED] = (prefs[KEY_CONCEPTS_REVIEWED] ?: 0) + 1
        }
    }

    suspend fun incrementQuizzesCompleted() {
        dataStore.edit { prefs ->
            prefs[KEY_QUIZZES_COMPLETED] = (prefs[KEY_QUIZZES_COMPLETED] ?: 0) + 1
        }
    }

    suspend fun incrementPdfAnswers() {
        dataStore.edit { prefs ->
            prefs[KEY_PDF_ANSWERS] = (prefs[KEY_PDF_ANSWERS] ?: 0) + 1
        }
    }
}
