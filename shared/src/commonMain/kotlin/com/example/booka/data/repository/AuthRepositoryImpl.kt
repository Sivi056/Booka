package com.example.booka.data.repository

import com.example.booka.domain.model.User
import com.example.booka.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl : AuthRepository {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override val currentUser: Flow<User?> = auth.authStateChanged.map { firebaseUser ->
        firebaseUser?.let {
            User(
                uid = it.uid,
                email = it.email ?: "",
                displayName = it.displayName ?: ""
            )
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        role: String
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password)
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("User creation failed"))

            val newUser = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = role
            )

            // Save user profile & role to Firestore
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(User.serializer(), newUser)

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password)
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Sign in failed"))

            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()

            val user = userDoc.data(User.serializer())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
