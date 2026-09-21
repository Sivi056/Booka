package com.example.booka.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String = "",
    val serviceName: String = "",
    val clientName: String = "",
    val date: String = "",
    val status: String = "PENDING"
)