package com.concept60.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.concept60.app.data.model.TrendingConcept
import com.concept60.app.ui.Screen
import com.concept60.app.ui.components.*
import com.concept60.app.ui.theme.Accent
import java.util.Calendar

private val DAILY_CONCEPTS = listOf(
    TrendingConcept("Quantum Entanglement", 0, "", "Science"),
    TrendingConcept("Machine Learning", 0, "", "Technology"),
    TrendingConcept("Supply and Demand", 0, "", "Business"),
    TrendingConcept("Pythagorean Theorem", 0, "", "Mathematics"),
    TrendingConcept("Photosynthesis", 0, "", "Science"),
    TrendingConcept("The Great Depression", 0, "", "History"),
    TrendingConcept("Blockchain", 0, "", "Technology"),
    TrendingConcept("Cell Division", 0, "", "Science"),
    TrendingConcept("Interest Rates", 0, "", "Finance")
)

@Composable
fun HomeScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    
    // Concept of the day - changes every 24 hours
    val conceptOfDay = remember {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val index = (dayOfYear + year) % DAILY_CONCEPTS.size
        DAILY_CONCEPTS[index]
    }

    fun navigate(concept: String, category: String = "General") {
        navController.navigate(Screen.Result.createRoute(concept, category))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        // ── Hero / Search ─────────────────────────────────────────────────────
        PanelCard {
            OverlineLabel("Concept in 60 Seconds")
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Learn any concept with calm, clear AI explanations.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Type a topic and instantly receive a beginner-friendly summary, analogy, and key takeaway.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search a concept…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboard?.hide()
                    if (query.isNotBlank()) navigate(query.trim())
                }),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    keyboard?.hide()
                    if (query.isNotBlank()) navigate(query.trim())
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                enabled = query.isNotBlank(),
            ) {
                Text("Explain this concept", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── Concept of the day ────────────────────────────────────────────────
        PanelCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                Spacer(Modifier.width(8.dp))
                OverlineLabel("Concept of the day")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                conceptOfDay.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Expand your knowledge today with this featured topic curated just for you.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { navigate(conceptOfDay.name, conceptOfDay.category) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Text("Learn this concept", color = Color.White)
            }
        }

        // ── Recent Activity / Quick Links ─────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier = Modifier.weight(1f).clickable { navController.navigate(Screen.Trending.route) },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Accent)
                    Spacer(Modifier.height(8.dp))
                    Text("Trending", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Popular now", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(
                modifier = Modifier.weight(1f).clickable { navController.navigate(Screen.Saved.route) },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Accent)
                    Spacer(Modifier.height(8.dp))
                    Text("Saved", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Your library", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
