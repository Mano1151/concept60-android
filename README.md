# Concept60 — Native Android App (Kotlin + Jetpack Compose)

A full native Android rewrite of the Concept in 60 Seconds app. Replaces Capacitor with real Kotlin, Jetpack Compose UI, and proper Android architecture.

---

## Tech stack

| Layer | Library |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Dependency injection | Hilt |
| Networking | Retrofit 2 + OkHttp |
| Auth | Firebase Auth |
| Cloud data | Firestore (via backend API) |
| Local storage | DataStore Preferences |
| Image loading | Coil |
| Video playback | Media3 / ExoPlayer |
| Build | Gradle with version catalog (libs.versions.toml) |

---

## Project structure

```
app/src/main/java/com/concept60/app/
├── Concept60App.kt              # Hilt application class
├── MainActivity.kt              # Single activity entry point
│
├── data/
│   ├── api/
│   │   ├── Concept60Api.kt      # Retrofit interface (all endpoints)
│   │   └── AppModule.kt         # Hilt module: Retrofit, OkHttp, Firebase
│   ├── model/
│   │   └── Models.kt            # All data classes
│   └── repository/
│       ├── AuthRepository.kt    # Firebase Auth wrapper
│       ├── ConceptRepository.kt # API calls + response normalization
│       ├── SavedConceptsRepository.kt  # DataStore + cloud saved searches
│       └── SettingsRepository.kt       # Accessibility settings (DataStore)
│
├── viewmodel/
│   ├── AuthViewModel.kt         # Login / signup / password reset
│   ├── ConceptViewModel.kt      # Result screen — concept + video loading
│   ├── PdfQaViewModel.kt        # PDF upload, text extraction, Q&A
│   └── OtherViewModels.kt       # SavedViewModel, SettingsViewModel
│
└── ui/
    ├── Navigation.kt            # NavHost + all routes
    ├── theme/
    │   └── Theme.kt             # Colors, typography, dark/light themes
    ├── components/
    │   └── Components.kt        # PanelCard, chips, skeleton, bottom nav, etc.
    └── screens/
        ├── HomeScreen.kt        # Search, categories, trending, concept of day
        ├── ResultScreen.kt      # Explanation, scenarios, flashcards, quiz
        ├── AuthScreens.kt       # Login, Signup, ForgotPassword
        ├── OtherScreens.kt      # Saved, Trending, Categories, Settings, Profile, PdfQA
```

---

## Setup

### 1. Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 26+ (minSdk), target SDK 35

### 2. Clone / open

```bash
# Open the concept60-kotlin/ folder in Android Studio
# File → Open → select the concept60-kotlin directory
```

### 3. Add your Firebase config

1. Go to [Firebase Console](https://console.firebase.google.com/) → your existing project
2. Add an Android app with package name `com.concept60.app`
3. Download `google-services.json`
4. Place it at `app/google-services.json`

This file is required — the build will fail without it.

### 4. Enable Firebase services

In the Firebase console, make sure these are enabled:
- **Authentication** → Email/Password sign-in method
- **Firestore Database** (if you use cloud saved searches)

### 5. Backend URL

The app points to `https://concept60-1.onrender.com/` (same as the React app).

To change it, edit `AppModule.kt`:
```kotlin
private const val BASE_URL = "https://concept60-1.onrender.com/"
```

### 6. Build and run

In Android Studio:
- Click **Run ▶** or press `Shift+F10`
- Choose your device or emulator (API 26+)

Or via command line:
```bash
./gradlew assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```

---

## Screens

| Screen | Route | Description |
|---|---|---|
| Home | `home` | Search bar, category chips, trending, concept of the day |
| Result | `result/{concept}/{category}` | One-liner, scenario, examples, flashcards, quiz, share |
| Saved | `saved` | Local + cloud saved concepts with delete |
| Trending | `trending` | Ranked list of trending searches |
| Categories | `categories` | Browse all topic categories |
| PDF Q&A | `pdf_qa` | Upload PDF, ask natural language questions |
| Profile | `profile` | User info, saved count, edit name, sign out |
| Settings | `settings` | Font size, reading mode, TTS speed, theme |
| Login | `login` | Email/password login |
| Signup | `signup` | Email/password account creation |
| Forgot Password | `forgot_password` | Firebase password reset email |

---

## Key differences from the Capacitor app

| Feature | React + Capacitor | Kotlin native |
|---|---|---|
| UI rendering | WebView (HTML/CSS) | Native Views via Compose |
| Navigation | React Router | Navigation Compose |
| State | React hooks + Context | ViewModel + StateFlow |
| Local storage | localStorage | DataStore Preferences |
| HTTP client | Axios | Retrofit + OkHttp |
| Auth token | Firebase JS SDK | Firebase Android SDK |
| PDF text extraction | pdfjs-dist (JS) | ContentResolver + stream |
| Google Sign-In | Capacitor plugin | Add `firebase-ui-auth` if needed |
| Performance | JS bridge overhead | Full native performance |

---

## Adding Google Sign-In (optional)

Google Sign-In requires a SHA-1 fingerprint registered in Firebase.

1. Get your debug SHA-1:
   ```bash
   ./gradlew signingReport
   ```
2. Add it to Firebase Console → Project settings → Your Android app
3. Add the dependency in `app/build.gradle.kts`:
   ```kotlin
   implementation("com.google.firebase:firebase-auth-ktx")
   implementation("com.google.android.gms:play-services-auth:21.2.0")
   ```
4. In `AuthRepository.kt`, add a `signInWithGoogle(idToken: String)` method using `GoogleAuthProvider.getCredential(idToken, null)`

---

## PDF text extraction note

The current implementation uses a basic stream reader that works well for text-based PDFs. For scanned/image PDFs, consider adding ML Kit's text recognition:

```kotlin
// build.gradle.kts
implementation("com.google.mlkit:text-recognition:16.0.0")
```

---

## Release build

1. Create a signing keystore:
   ```bash
   keytool -genkey -v -keystore concept60.jks -keyalg RSA -keysize 2048 -validity 10000 -alias concept60
   ```
2. Add signing config to `app/build.gradle.kts` under `buildTypes.release`
3. Build:
   ```bash
   ./gradlew assembleRelease
   ```
