package com.example.booka.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ServiceItem(
    val id: String = "",
    val providerId: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val durationMinutes: Int = 60
)