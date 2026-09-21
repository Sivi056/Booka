package com.example.booka.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "client" // "client" or "provider"
)