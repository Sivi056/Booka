package com.example.booka.presentation.booking

import com.example.booka.domain.model.Booking
import com.example.booka.domain.model.ServiceItem

sealed interface BookingUiState {
    data object Loading : BookingUiState
    data class Success(
        val services: List<ServiceItem> = emptyList(),
        val userBookings: List<Booking> = emptyList()
    ) : BookingUiState
    data class Error(val message: String) : BookingUiState
}