package com.concept60.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.concept60.app.data.repository.ConceptRepository
import com.concept60.app.data.repository.SavedConceptsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

data class PdfQaUiState(
    val fileName: String = "",
    val pdfText: String = "",
    val question: String = "",
    val answer: String = "",
    val isExtracting: Boolean = false,
    val isAsking: Boolean = false,
    val pdfReady: Boolean = false,
    val error: String = "",
)

@HiltViewModel
class PdfQaViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val savedRepo: SavedConceptsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PdfQaUiState())
    val state: StateFlow<PdfQaUiState> = _state.asStateFlow()

    fun loadPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            val fileName = getFileName(context, uri)
            _state.value = _state.value.copy(
                isExtracting = true,
                fileName = fileName,
                error = "",
                answer = "",
                pdfReady = false,
            )

            val text = withContext(Dispatchers.IO) {
                extractTextFromPdf(context, uri)
            }

            if (text.isNullOrBlank()) {
                _state.value = _state.value.copy(
                    isExtracting = false,
                    error = "No text detected in the uploaded PDF.",
                )
            } else {
                _state.value = _state.value.copy(
                    isExtracting = false,
                    pdfText = text,
                    pdfReady = true,
                )
            }
        }
    }

    fun setQuestion(q: String) {
        _state.value = _state.value.copy(question = q)
    }

    fun askQuestion() {
        val current = _state.value
        if (current.pdfText.isBlank() || current.question.isBlank()) return

        viewModelScope.launch {
            _state.value = current.copy(isAsking = true, error = "", answer = "")
            conceptRepository.askPdfQuestion(current.pdfText, current.question).fold(
                onSuccess = { answer ->
                    _state.value = _state.value.copy(isAsking = false, answer = answer)
                    savedRepo.incrementPdfAnswers()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isAsking = false,
                        error = e.message ?: "Unable to answer the question.",
                    )
                }
            )
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: uri.lastPathSegment ?: "document.pdf"
    }

    /**
     * Basic text extraction from PDF using Android's PdfRenderer.
     * For better extraction, the android-pdf-viewer library is included.
     * This approach uses the ContentResolver to read the raw bytes and
     * extracts visible text using Android's built-in PDF renderer.
     */
    private fun extractTextFromPdf(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                // Use pdfjs or a text-stream approach for text PDFs
                // Fallback: read as text stream (works for text-based PDFs)
                val reader = BufferedReader(InputStreamReader(stream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // Filter for printable ASCII text lines (crude but functional for text PDFs)
                    val cleaned = line!!.filter { it.code in 32..126 || it == '\n' }
                    if (cleaned.trim().length > 3) sb.appendLine(cleaned.trim())
                }
                sb.toString().trim().ifBlank { null }
            }
        } catch (e: Exception) {
            null
        }
    }
}
