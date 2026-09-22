package com.example.booka.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "client",
    val displayName: String = ""
) {
    val id: String get() = uid
}