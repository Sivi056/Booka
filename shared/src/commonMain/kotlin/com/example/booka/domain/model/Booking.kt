package com.example.booka.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class Booking(
    val id: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val clientId: String = "",
    val providerId: String = "",
    val dateTimeStamp: Long = 0L,
    val status: String = "PENDING", // PENDING, CONFIRMED, CANCELLED
    val price: Double = 0.0
)
