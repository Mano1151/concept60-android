package com.concept60.app.data.repository

import com.concept60.app.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    val currentUser: Flow<UserProfile?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(
                user?.let {
                    UserProfile(it.uid, it.email, it.displayName, it.photoUrl?.toString())
                }
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Login failed: User not found"))
            val profile = UserProfile(user.uid, user.email, user.displayName, user.photoUrl?.toString())
            saveUserInfoToFirestore(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Signup failed: User not created"))
            val profile = UserProfile(user.uid, user.email, user.displayName, user.photoUrl?.toString())
            saveUserInfoToFirestore(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveUserInfoToFirestore(profile: UserProfile) {
        try {
            android.util.Log.d("Firestore", "Saving user profile to Firestore: ${profile.uid}")
            firestore.collection("users")
                .document(profile.uid)
                .set(profile)
                .await()
            android.util.Log.d("Firestore", "User profile saved successfully")
        } catch (e: Exception) {
            android.util.Log.e("Firestore", "Error saving user info to Firestore", e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(name: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name).build()
            user.updateProfile(profileUpdates).await()
            // Reload user to propagate changes to AuthStateListener
            user.reload().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() = auth.signOut()
}
