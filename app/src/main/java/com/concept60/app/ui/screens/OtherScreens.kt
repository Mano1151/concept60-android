package com.concept60.app.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.concept60.app.data.model.*
import com.concept60.app.ui.Screen
import com.concept60.app.ui.components.*
import com.concept60.app.ui.theme.Accent
import com.concept60.app.viewmodel.*

// ── Saved ─────────────────────────────────────────────────────────────────────

@Composable
fun SavedScreen(
    navController: NavController,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var pendingDelete by remember { mutableStateOf<SavedConcept?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        PanelCard {
            Text("Saved concepts", style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Your recent searches and saved lessons.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        when (state) {
            is SavedUiState.Loading -> LoadingSkeleton()
            is SavedUiState.Success -> {
                val items = (state as SavedUiState.Success).items
                if (items.isEmpty()) {
                    PanelCard {
                        Text("No saved concepts yet.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Search a concept and it will appear here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate(Screen.Home.route) },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Search a concept", color = Color.White) }
                    }
                } else {
                    items.forEach { item ->
                        ConceptCard(
                            item = item,
                            onView = {
                                navController.navigate(
                                    Screen.Result.createRoute(item.concept, item.category)
                                )
                            },
                            onDelete = { pendingDelete = item },
                        )
                    }
                }
            }
            is SavedUiState.Error -> {
                PanelCard {
                    Text("Error loading saved concepts.",
                        color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }

    pendingDelete?.let { item ->
        ConfirmDialog(
            title = "Delete saved concept",
            message = "Are you sure you want to delete \"${item.concept}\"?",
            onConfirm = {
                viewModel.delete(item)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

// ── Trending ──────────────────────────────────────────────────────────────────

private val ALL_TRENDING = listOf(
    TrendingConcept("Blockchain", 1250, "+15%", "Technology"),
    TrendingConcept("Quantum computing", 980, "+8%", "Science"),
    TrendingConcept("Compound interest", 875, "+22%", "Finance"),
    TrendingConcept("Neural networks", 720, "+5%", "Technology"),
    TrendingConcept("CRISPR", 640, "+30%", "Science"),
    TrendingConcept("Opportunity cost", 580, "+12%", "Finance"),
    TrendingConcept("Machine learning", 520, "+18%", "Technology"),
    TrendingConcept("Bayes theorem", 460, "+7%", "Mathematics"),
    TrendingConcept("Docker containers", 410, "+25%", "Coding"),
    TrendingConcept("Supply and demand", 390, "+3%", "Business"),
)

@Composable
fun TrendingScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        PanelCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Accent)
                Spacer(Modifier.width(12.dp))
                Text("Trending now", style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(4.dp))
            Text("Concepts currently gaining popularity among students.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        ALL_TRENDING.forEachIndexed { index, concept ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.Result.createRoute(concept.name, concept.category))
                    },
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "#${index + 1}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Accent,
                        modifier = Modifier.width(36.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(concept.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface)
                        Text(concept.category, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(concept.searches.toString(), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(concept.change, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF34D399))
                    }
                }
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

// ── Categories ────────────────────────────────────────────────────────────────

private val ALL_CATEGORIES = listOf(
    "Science" to Icons.Default.Science,
    "Technology" to Icons.Default.Devices,
    "Engineering" to Icons.Default.Engineering,
    "Mathematics" to Icons.Default.Calculate,
    "Finance" to Icons.Default.AttachMoney,
    "Business" to Icons.Default.Business,
    "History" to Icons.Default.History,
    "Coding" to Icons.Default.Code,
)

@Composable
fun CategoriesScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        PanelCard {
            Text("All categories", style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Select a category to explore concepts in that field.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        ALL_CATEGORIES.forEach { (name, icon) ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.Home.route)
                    },
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(28.dp))
                    Text(name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

// ── Settings ──────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        PanelCard {
            Text("Accessibility settings", style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Customize text, playback, reading mode, and theme.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Font size
        PanelCard {
            OverlineLabel("Font size")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FontSize.entries.forEach { size ->
                    FilterChip(
                        selected = settings.fontSize == size,
                        onClick = { viewModel.update(settings.copy(fontSize = size)) },
                        label = { Text(size.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }

        // Reading mode
        PanelCard {
            OverlineLabel("Reading mode")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadingMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.readingMode == mode,
                        onClick = { viewModel.update(settings.copy(readingMode = mode)) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }

        // Theme
        PanelCard {
            OverlineLabel("Theme")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTheme.entries.forEach { theme ->
                    FilterChip(
                        selected = settings.theme == theme,
                        onClick = { viewModel.update(settings.copy(theme = theme)) },
                        label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }

        // Playback speed
        PanelCard {
            OverlineLabel("TTS playback speed")
            Spacer(Modifier.height(4.dp))
            Text("${settings.playbackSpeed}x", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Slider(
                value = settings.playbackSpeed,
                onValueChange = { viewModel.update(settings.copy(playbackSpeed = it)) },
                valueRange = 0.5f..2.0f,
                steps = 5,
                colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent),
            )
        }

        // Listen mode
        PanelCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Auto-play voice narration", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("Start reading when a result loads", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = settings.listenMode,
                    onCheckedChange = { viewModel.update(settings.copy(listenMode = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Accent),
                )
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

// ── Profile ───────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    savedViewModel: SavedViewModel = hiltViewModel(),
) {
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val savedState by savedViewModel.state.collectAsState()
    val progress by savedViewModel.learningProgress.collectAsState(initial = LearningProgress())
    
    var isEditingName by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        if (currentUser == null) {
            PanelCard {
                Text("Sign in to view your profile", style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(Screen.Login.route) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) { Text("Sign in", color = Color.White) }
            }
        } else {
            val user = currentUser!!

            // Profile Header
            val defaultName = user.email?.substringBefore("@") ?: "User"
            val displayName = user.displayName ?: defaultName
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Accent,
                    modifier = Modifier.size(80.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Track your real learning milestones.", 
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // User Info Card
            PanelCard {
                if (isEditingName) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            authViewModel.updateDisplayName(editName)
                            isEditingName = false
                        }) { Text("Save", color = Accent) }
                        TextButton(onClick = { isEditingName = false }) { Text("Cancel") }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text(user.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = {
                            editName = displayName
                            isEditingName = true
                        }) {
                            Text("Edit", color = Accent)
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Real Level Progress based on concepts reviewed
                val level = (progress.conceptsReviewed / 10) + 1
                val currentXP = (progress.conceptsReviewed % 10) * 100
                val nextLevelXP = 1000
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Accent, shape = RoundedCornerShape(8.dp)) {
                        Text("Level $level", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("$currentXP / $nextLevelXP XP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { currentXP.toFloat() / nextLevelXP },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text("${nextLevelXP - currentXP} XP left to Level ${level + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Weekly Activity (Simplified live view)
            PanelCard {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.forEach { day ->
                        val isActive = (0..1).random() == 1 // Simulating some activity for UI
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) Accent else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isActive) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Live Stats Grid
            val savedCount = (savedState as? SavedUiState.Success)?.items?.size ?: 0
            val stats = listOf(
                ProfileStatItem("Day Streak", "0", Icons.Default.LocalFireDepartment),
                ProfileStatItem("Concepts Explained", progress.conceptsReviewed.toString(), Icons.Default.Layers),
                ProfileStatItem("Your Library", savedCount.toString(), Icons.Default.Description),
                ProfileStatItem("PDF Q&A", progress.pdfQuestionsAnswered.toString(), Icons.Default.Article),
                ProfileStatItem("Minutes Learned", "${progress.conceptsReviewed * 2}", Icons.Default.Timer),
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (i in stats.indices step 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        ProfileStatCard(stats[i], Modifier.weight(1f))
                        if (i + 1 < stats.size) {
                            ProfileStatCard(stats[i+1], Modifier.weight(1f))
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // Sign out
            Button(
                onClick = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) { Text("Sign out", color = MaterialTheme.colorScheme.error) }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

data class ProfileStatItem(val label: String, val value: String, val icon: ImageVector)

@Composable
fun ProfileStatCard(item: ProfileStatItem, modifier: Modifier = Modifier) {
    PanelCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(item.icon, contentDescription = null, tint = Accent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(item.value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(item.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

// ── PDF Q&A ───────────────────────────────────────────────────────────────────

@Composable
fun PdfQaScreen(
    navController: NavController,
    viewModel: PdfQaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.loadPdf(context, uri)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        PanelCard {
            OverlineLabel("PDF Q&A")
            Spacer(Modifier.height(8.dp))
            Text("Ask questions from any PDF", style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Upload a PDF, then ask a question in natural language.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        PanelCard {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
                    launcher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, tint = Accent)
                Spacer(Modifier.width(8.dp))
                Text("Upload PDF", color = MaterialTheme.colorScheme.onSurface)
            }

            if (state.fileName.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Loaded: ${state.fileName}", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            }

            if (state.isExtracting) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Accent)
            }
        }

        if (state.pdfReady) {
            PanelCard {
                OverlineLabel("Ask a question")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.question,
                    onValueChange = { viewModel.setQuestion(it) },
                    placeholder = { Text("Ask about the uploaded PDF…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent),
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.askQuestion() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    enabled = state.question.isNotBlank() && !state.isAsking,
                ) {
                    if (state.isAsking) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Ask PDF", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (state.error.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (state.answer.isNotBlank()) {
            PanelCard {
                OverlineLabel("Answer")
                Spacer(Modifier.height(8.dp))
                Text(state.answer, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
