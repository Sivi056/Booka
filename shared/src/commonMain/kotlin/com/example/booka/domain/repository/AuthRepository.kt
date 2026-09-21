package com.example.booka.domain.repository

import com.example.booka.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signUp(email: String, password: String, displayName: String, role: String): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signOut()
}