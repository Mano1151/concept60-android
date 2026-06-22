package com.concept60.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.concept60.app.data.model.ConceptResult
import com.concept60.app.data.model.Keyword
import com.concept60.app.ui.Screen
import com.concept60.app.ui.components.*
import com.concept60.app.ui.theme.Accent
import com.concept60.app.viewmodel.*

@Composable
fun ResultScreen(
    concept: String,
    category: String,
    navController: NavController,
    viewModel: ConceptViewModel = hiltViewModel(),
) {
    val conceptState by viewModel.conceptState.collectAsState()
    val videoState by viewModel.videoState.collectAsState()
    val lessonSaved by viewModel.lessonSaved.collectAsState()
    val quizCompleted by viewModel.quizCompleted.collectAsState()
    val progress by viewModel.learningProgress.collectAsState(initial = null)
    val context = LocalContext.current

    var showQuizAnswers by remember { mutableStateOf(false) }

    LaunchedEffect(concept, category) {
        viewModel.loadConcept(concept, category)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        // ── Back button ───────────────────────────────────────────────────────
        TextButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Back")
        }

        when (val state = conceptState) {
            is ConceptUiState.Loading -> {
                LoadingInteractive()
            }

            is ConceptUiState.Error -> {
                PanelCard {
                    val isAuthError = state.message.contains("401") || state.message.contains("unauthorized", ignoreCase = true)
                    Text(
                        if (isAuthError) "Sign in required" else "Something went wrong",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isAuthError) "Please sign in to access AI explanations." else state.message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (isAuthError) navController.navigate(Screen.Login.route)
                            else navController.popBackStack()
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    ) {
                        Text(if (isAuthError) "Go to Sign In" else "Search again", color = Color.White)
                    }
                }
            }

            is ConceptUiState.Success -> {
                val result = state.result
                val fullText = buildFullText(result)

                // ── Concept overview ──────────────────────────────────────────
                PanelCard {
                    OverlineLabel("Concept overview")
                    Spacer(Modifier.height(8.dp))
                    Text(result.concept, style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                    if (result.oneLiner.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(result.oneLiner,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OverlineLabel("Real-world scenario")
                            Spacer(Modifier.height(8.dp))
                            Text(result.scenario, style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface)
                            if (result.keywords.isNotEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                OverlineLabel("Keywords")
                                Spacer(Modifier.height(8.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    result.keywords.forEach { kw -> KeywordChip(kw.term) }
                                }
                            }
                        }
                    }
                }

                // ── Example scenarios ─────────────────────────────────────────
                if (result.exampleScenarios.isNotEmpty()) {
                    PanelCard {
                        OverlineLabel("More examples")
                        Spacer(Modifier.height(12.dp))
                        result.exampleScenarios.forEachIndexed { index, example ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    OverlineLabel("Example ${index + 1}")
                                    Spacer(Modifier.height(6.dp))
                                    Text(example, style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // ── Flashcards ────────────────────────────────────────────────
                if (result.keywords.isNotEmpty()) {
                    PanelCard {
                        OverlineLabel("Keyword flashcards")
                        Spacer(Modifier.height(4.dp))
                        Text("Keyword flashcards", style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        result.keywords.forEach { kw -> FlashCard(kw) }
                    }
                }

                // ── Deep Dive: Q&A ──────────────────────────────────────────
                val qaPairs = remember(result) {
                    val base = result.quiz.map { it.question to it.answer }.toMutableList()
                    if (base.size < 5) {
                        val fallbacks = quizQuestions(result)
                        var i = 0
                        while (base.size < 5 && i < fallbacks.size) {
                            val pair = fallbacks[i]
                            if (base.none { it.first == pair.first }) {
                                base.add(pair)
                            }
                            i++
                        }
                    }
                    base.take(8)
                }

                PanelCard {
                    OverlineLabel("Deep Dive: Q&A")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Frequently asked questions about ${result.concept}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    qaPairs.forEachIndexed { index, (question, answer) ->
                        var expanded by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { expanded = !expanded }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Q${index + 1}: $question",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = Accent
                                    )
                                }
                                AnimatedVisibility(visible = expanded) {
                                    Column {
                                        Spacer(Modifier.height(12.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            answer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Learning progress ─────────────────────────────────────────
                progress?.let { prog ->
                    PanelCard {
                        OverlineLabel("Learning progress")
                        Spacer(Modifier.height(8.dp))
                        Text("Track your lesson activity",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ProgressStat("Concepts", prog.conceptsReviewed.toString(), Modifier.weight(1f))
                            ProgressStat("PDF Q&A", prog.pdfQuestionsAnswered.toString(), Modifier.weight(1f))
                        }
                    }
                }

                // ── Quick actions ─────────────────────────────────────────────
                PanelCard {
                    OverlineLabel("Actions")
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.saveLesson(concept, category, result)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Accent),
                            enabled = !lessonSaved,
                        ) {
                            Text(if (lessonSaved) "Lesson saved ✓" else "Save lesson", color = Accent)
                        }
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Concept", fullText))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Copy explanation")
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TITLE, result.concept)
                                    putExtra(Intent.EXTRA_TEXT, fullText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share concept"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Share concept")
                        }
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        ) { Text("Search again", color = Color.White) }
                    }
                }
            }

            else -> {}
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
fun LoadingInteractive() {
    val messages = listOf(
        "AI is analyzing the core principles...",
        "Crafting a clear analogy for you...",
        "Simplifying complex terms...",
        "Structuring your 60-second summary...",
        "Almost ready! Just a few more seconds..."
    )
    var index by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(2500)
            index = (index + 1) % messages.size
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Accent, strokeWidth = 4.dp)
        Spacer(Modifier.height(24.dp))
        Text(
            messages[index],
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.animateContentSize()
        )
        Spacer(Modifier.height(48.dp))
        LoadingSkeleton()
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun buildFullText(result: ConceptResult): String {
    val examples = if (result.exampleScenarios.isNotEmpty())
        "\n\nExamples:\n" + result.exampleScenarios.mapIndexed { i, e -> "${i + 1}. $e" }.joinToString("\n")
    else ""
    val keywords = if (result.keywords.isNotEmpty())
        "\n\nKeywords: " + result.keywords.joinToString(", ") { it.term }
    else ""
    return "${result.concept}\n\n${result.scenario}$examples$keywords"
}

private fun quizQuestions(result: ConceptResult): List<Pair<String, String>> = listOf(
    "What real-world scenario explains ${result.concept}?" to result.scenario,
    "Which keyword best captures the main idea of ${result.concept}?" to
        (result.keywords.firstOrNull()?.term ?: "Review the keywords above."),
    "What is one practical use case for ${result.concept}?" to
        (result.exampleScenarios.firstOrNull() ?: result.scenario),
    "How does ${result.concept} apply to beginners?" to result.oneLiner,
    "What is a key takeaway about ${result.concept}?" to result.scenario.take(100),
    "Identify a related concept to ${result.concept}." to (result.keywords.lastOrNull()?.term ?: "General Knowledge"),
    "True or False: ${result.concept} is only for experts." to "False",
    "Where might you encounter ${result.concept} in daily life?" to result.scenario,
    "Summary of ${result.concept} in one word." to (result.keywords.getOrNull(1)?.term ?: result.concept),
    "Challenge: Explain ${result.concept} to a friend." to result.oneLiner
)

@Composable
private fun FlashCard(keyword: Keyword) {
    var flipped by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { flipped = !flipped },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OverlineLabel(keyword.term)
            AnimatedVisibility(visible = flipped) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        keyword.definition.ifBlank { "A key idea connected to this concept." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (!flipped) {
                Spacer(Modifier.height(4.dp))
                Text("Tap to reveal definition", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun QuizQuestion(index: Int, question: String, answer: String, showAnswer: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OverlineLabel("Question $index")
            Spacer(Modifier.height(6.dp))
            Text(question, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            AnimatedVisibility(visible = showAnswer) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Answer:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Text(answer, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}
