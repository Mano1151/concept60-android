package com.concept60.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.concept60.app.R
import com.concept60.app.ui.Screen
import com.concept60.app.ui.components.OverlineLabel
import com.concept60.app.ui.components.PanelCard
import com.concept60.app.ui.theme.Accent
import com.concept60.app.viewmodel.AuthState
import com.concept60.app.viewmodel.AuthViewModel

// ── Login ─────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(initial = null)
    var isNavigating by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser, authState) {
        if (!isNavigating && (currentUser != null || authState is AuthState.Success)) {
            isNavigating = true
            // Clear everything back to the Home screen
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.mipmap.concept60_foreground),
            contentDescription = "Concept60 Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF3D65DC)),
            contentScale = ContentScale.Inside
        )
        Spacer(Modifier.height(8.dp))

        PanelCard {
            Text(
                "Welcome back", style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Sign in to access your saved concepts and progress.",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent),
            )
            Spacer(Modifier.height(12.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent),
            )

            var rememberMe by remember { mutableStateOf(true) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = Accent)
                )
                Text("Remember me", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = { navController.navigate(Screen.ForgotPassword.route) },
                    contentPadding = PaddingValues(0.dp),
                ) { Text("Forgot password?", color = Accent) }
            }

            Spacer(Modifier.height(16.dp))

            if (authState is AuthState.Error) {
                Text(
                    (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.signInWithEmail(email, password) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                enabled = email.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading,
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign in", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(
                onClick = { navController.navigate(Screen.Signup.route) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Sign up", color = Accent, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

// ── Signup ────────────────────────────────────────────────────────────────────

@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(initial = null)
    var isNavigating by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf("") }

    LaunchedEffect(currentUser, authState) {
        if (!isNavigating && (currentUser != null || authState is AuthState.Success)) {
            isNavigating = true
            // Clear everything back to the Home screen
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.mipmap.concept60_foreground),
            contentDescription = "Concept60 Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF3D65DC)),
            contentScale = ContentScale.Inside
        )
        Spacer(Modifier.height(8.dp))

        PanelCard {
            Text(
                "Create your account",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Start saving concepts and tracking your learning progress.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent),
            )
            Spacer(Modifier.height(16.dp))

            val errorMsg = localError.ifEmpty { (authState as? AuthState.Error)?.message ?: "" }
            if (errorMsg.isNotEmpty()) {
                Text(
                    errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    localError = ""
                    if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        localError = "Please fill in all fields."
                    } else if (password != confirmPassword) {
                        localError = "Passwords do not match."
                    } else {
                        viewModel.signUpWithEmail(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                enabled = authState !is AuthState.Loading,
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create account", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(
                onClick = { navController.navigate(Screen.Login.route) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Sign in", color = Accent, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

// ── Forgot password ───────────────────────────────────────────────────────────

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        PanelCard {
            Text("Reset your password", style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Enter your email and we'll send you a reset link.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent),
            )
            Spacer(Modifier.height(16.dp))

            when (authState) {
                is AuthState.Success -> {
                    Text("Reset email sent! Check your inbox.",
                        color = Color(0xFF34D399), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
                is AuthState.Error -> {
                    Text((authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
                else -> {}
            }

            Button(
                onClick = { viewModel.sendPasswordReset(email) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                enabled = email.isNotBlank() && authState !is AuthState.Loading,
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Send reset email", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Back to sign in", color = Accent)
            }
        }
    }
}
