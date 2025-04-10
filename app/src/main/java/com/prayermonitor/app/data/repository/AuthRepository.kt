package com.prayermonitor.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.prayermonitor.app.data.database.UserDao
import com.prayermonitor.app.data.model.User
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepository(private val userDao: UserDao) {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    suspend fun login(email: String, password: String): FirebaseUser {
        return suspendCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    if (firebaseUser != null) {
                        continuation.resume(firebaseUser)
                    } else {
                        continuation.resumeWithException(Exception("Login failed"))
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }
    
    suspend fun register(email: String, password: String, name: String): FirebaseUser {
        return suspendCoroutine { continuation ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    if (firebaseUser != null) {
                        // Create user in local database
                        val user = User(
                            id = firebaseUser.uid,
                            email = email,
                            name = name
                        )
                        // This would typically be done in a coroutine, but for simplicity
                        // we're just showing the concept here
                        continuation.resume(firebaseUser)
                    } else {
                        continuation.resumeWithException(Exception("Registration failed"))
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }
    
    suspend fun createUserInDatabase(user: User) {
        userDao.insert(user)
    }
    
    fun logout() {
        firebaseAuth.signOut()
    }
    
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    suspend fun resetPassword(email: String) {
        return suspendCoroutine { continuation ->
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }
}
