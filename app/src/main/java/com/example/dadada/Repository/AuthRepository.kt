package com.example.dadada.Repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val usersPublicRef = FirebaseDatabase.getInstance(
        "https://cs330-pz-6310-default-rtdb.europe-west1.firebasedatabase.app/"
    ).getReference("UsersPublic")

    data class AuthResult(
        val success: Boolean,
        val message: String,
        val userId: String? = null
    )

    fun register(
        username: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        val safeUsername = username.trim()
        if (safeUsername.isBlank()) {
            onResult(AuthResult(false, "Username is required"))
            return
        }
        val authEmail = usernameToAuthEmail(safeUsername)
        firebaseAuth.createUserWithEmailAndPassword(authEmail, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId.isNullOrBlank()) {
                    onResult(AuthResult(false, "Failed to resolve user id"))
                    return@addOnSuccessListener
                }
                val profile = UserAccount(
                    id = userId,
                    username = safeUsername,
                    usernameKey = usernameToKey(safeUsername),
                    createdAt = System.currentTimeMillis()
                )
                usersPublicRef.child(userId).setValue(profile)
                    .addOnSuccessListener {
                        onResult(AuthResult(true, "Registered successfully", userId = userId))
                    }
                    .addOnFailureListener { error ->
                        onResult(AuthResult(false, error.message ?: "Failed to save user profile"))
                    }
            }
            .addOnFailureListener { error ->
                onResult(AuthResult(false, mapAuthError(error, forRegister = true)))
            }
    }

    fun login(
        username: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        val safeUsername = username.trim()
        if (safeUsername.isBlank()) {
            onResult(AuthResult(false, "Username is required"))
            return
        }
        val authEmail = usernameToAuthEmail(safeUsername)
        firebaseAuth.signInWithEmailAndPassword(authEmail, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId.isNullOrBlank()) {
                    onResult(AuthResult(false, "Failed to resolve user id"))
                    return@addOnSuccessListener
                }

                ensurePublicProfile(userId, safeUsername)
                onResult(AuthResult(true, "Login successful", userId = userId))
            }
            .addOnFailureListener { error ->
                onResult(AuthResult(false, mapAuthError(error, forRegister = false)))
            }
    }

    fun getCurrentUser(
        onResult: (UserAccount?, String?) -> Unit
    ) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            onResult(null, "User is not authenticated")
            return
        }

        usersPublicRef.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserAccount::class.java)
                    if (user != null) {
                        if (user.id.isBlank()) {
                            user.id = snapshot.key ?: currentUser.uid
                        }
                        if (user.usernameKey.isBlank()) {
                            user.usernameKey = usernameToKey(user.username)
                        }
                        onResult(user, null)
                        return
                    }

                    val inferredUsername = currentUser.email
                        ?.substringBefore('@')
                        ?.replace('_', ' ')
                        ?.trim()
                        .orEmpty()
                        .ifBlank { "user" }
                    val fallbackUser = UserAccount(
                        id = currentUser.uid,
                        username = inferredUsername,
                        usernameKey = usernameToKey(inferredUsername),
                        createdAt = System.currentTimeMillis()
                    )
                    usersPublicRef.child(currentUser.uid).setValue(fallbackUser)
                    onResult(fallbackUser, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(null, error.message)
                }
            })
    }

    fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun currentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun ensurePublicProfile(
        userId: String,
        preferredUsername: String
    ) {
        val usernameKey = usernameToKey(preferredUsername)
        usersPublicRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val existing = snapshot.getValue(UserAccount::class.java)
                    if (existing != null && existing.username.isNotBlank()) return

                    val profile = UserAccount(
                        id = userId,
                        username = preferredUsername,
                        usernameKey = usernameKey,
                        createdAt = existing?.createdAt?.takeIf { it > 0L } ?: System.currentTimeMillis()
                    )
                    usersPublicRef.child(userId).setValue(profile)
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
    }

    private fun usernameToAuthEmail(username: String): String {
        return "${usernameToKey(username)}@dadada.local"
    }

    private fun mapAuthError(error: Exception, forRegister: Boolean): String {
        val messageUpper = error.message?.uppercase(Locale.ROOT).orEmpty()
        if (messageUpper.contains("CONFIGURATION_NOT_FOUND")) {
            return "Firebase Auth is not configured for this project. Enable Authentication and Email/Password in Firebase Console."
        }

        if (error is FirebaseAuthException) {
            if (error.errorCode == "ERROR_OPERATION_NOT_ALLOWED") {
                return "Email/Password sign-in is disabled. Enable it in Firebase Authentication providers."
            }
        }

        return when (error) {
            is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters"
            is FirebaseAuthUserCollisionException -> "Username already exists"
            is FirebaseAuthInvalidCredentialsException -> {
                if (forRegister) "Invalid username or password" else "Wrong username or password"
            }
            is FirebaseAuthInvalidUserException -> "User not found"
            else -> error.message ?: if (forRegister) "Registration failed" else "Login failed"
        }
    }

    private fun usernameToKey(username: String): String {
        return username
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9_]"), "_")
    }
}

data class UserAccount(
    var id: String = "",
    var username: String = "",
    var usernameKey: String = "",
    var createdAt: Long = 0L
)
