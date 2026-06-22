package com.concept60.app.data.repository

import com.concept60.app.data.api.Concept60Api
import com.concept60.app.data.model.*
import com.google.gson.internal.LinkedTreeMap
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConceptRepository @Inject constructor(
    private val api: Concept60Api,
) {
    suspend fun getConcept(concept: String, category: String): Result<ConceptResult> {
        return try {
            val response = retryOnColdStart { api.getConcept(ConceptRequest(concept, category)) }
            Result.success(response.toConceptResult())
        } catch (e: Exception) {
            val friendlyMessage = when {
                e is retrofit2.HttpException && e.code() == 401 -> "HTTP 401: Unauthorized"
                e is java.io.EOFException || e.message?.contains("End of input") == true ->
                    "The server is still waking up. Please try one more time in a few seconds."
                e is java.net.ConnectException -> "Cannot connect to server. Check your internet."
                else -> e.message ?: "An unexpected error occurred"
            }
            Result.failure(Exception(friendlyMessage))
        }
    }

    suspend fun getVideo(concept: String, category: String): Result<VideoResponse> {
        return try {
            val response = retryOnColdStart { api.getVideo(ConceptRequest(concept, category)) }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrending(): Result<List<TrendingConcept>> {
        return try {
            val response = retryOnColdStart { api.getTrending() }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper to automatically retry requests that fail due to backend "cold starts".
     */
    private suspend fun <T> retryOnColdStart(
        times: Int = 3,
        delayMillis: Long = 3000,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        repeat(times) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                val isColdStart = e is java.io.EOFException || e.message?.contains("End of input") == true
                if (!isColdStart || attempt == times - 1) {
                    throw e
                }
                delay(delayMillis)
            }
        }
        throw lastException ?: Exception("Unknown error during retry")
    }

    suspend fun askPdfQuestion(pdfText: String, question: String): Result<String> {
        return try {
            // Further reduced to 8,000 chars to ensure backend acceptance (HTTP 413 fix)
            val response = api.askPdfQuestion(
                PdfQuestionRequest(pdfText.take(8_000), question)
            )
            Result.success(response.answer.ifBlank { "No answer could be generated." })
        } catch (e: Exception) {
            val message = if (e.message?.contains("413") == true) {
                "PDF is too complex. Try a shorter section or a simpler document."
            } else {
                e.message ?: "Unable to answer from PDF."
            }
            Result.failure(Exception(message))
        }
    }

    // ── Normalize raw ConceptResponse ─────────────────────────────────────────

    private fun ConceptResponse.toConceptResult(): ConceptResult {
        fun String.valid(): String? = trim().takeIf { it.length >= 6 &&
            !it.contains(Regex("\\b(oops|sorry|error)\\b", RegexOption.IGNORE_CASE)) }

        val parsedKeywords = keywords.mapNotNull { item ->
            when (item) {
                is String -> if (item.trim().length > 2) Keyword(item.trim()) else null
                is Map<*, *> -> {
                    val term = (item["term"] ?: item["word"] ?: item["keyword"])
                        ?.toString()?.trim() ?: return@mapNotNull null
                    val def = (item["definition"] ?: item["desc"])?.toString()?.trim() ?: ""
                    if (term.isNotEmpty()) Keyword(term, def) else null
                }
                is LinkedTreeMap<*, *> -> {
                    val term = (item["term"] ?: item["word"] ?: item["keyword"])
                        ?.toString()?.trim() ?: return@mapNotNull null
                    val def = (item["definition"] ?: item["desc"])?.toString()?.trim() ?: ""
                    if (term.isNotEmpty()) Keyword(term, def) else null
                }
                else -> null
            }
        }

        return ConceptResult(
            concept = concept,
            oneLiner = oneLiner.valid() ?: "Definition not available — try again.",
            scenario = scenario.valid() ?: scenario,
            exampleScenarios = exampleScenarios.filter { it.trim().length > 2 },
            keywords = parsedKeywords,
            quiz = quiz ?: emptyList()
        )
    }
}
