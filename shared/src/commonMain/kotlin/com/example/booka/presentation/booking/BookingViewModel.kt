package com.example.booka.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booka.domain.model.Booking
import com.example.booka.domain.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.random.Random

class BookingViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Loading)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun loadServicesAndBookings(userId: String, isProvider: Boolean) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                bookingRepository.getAvailableServices()
                    .catch { e -> _uiState.value = BookingUiState.Error(e.message ?: "Failed to fetch services") }
                    .collect { services ->
                        bookingRepository.getBookingsForUser(userId, isProvider)
                            .catch { e -> _uiState.value = BookingUiState.Error(e.message ?: "Failed to fetch bookings") }
                            .collect { bookings ->
                                _uiState.value = BookingUiState.Success(
                                    services = services,
                                    userBookings = bookings
                                )
                            }
                    }
            } catch (e: Exception) {
                _uiState.value = BookingUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun bookService(serviceId: String, serviceTitle: String, clientId: String, providerId: String, price: Double) {
        viewModelScope.launch {
            val randomId = Random.nextLong(100000000000L, 999999999999L)
            val newBooking = Booking(
                id = "booking_$randomId",
                serviceId = serviceId,
                serviceTitle = serviceTitle,
                clientId = clientId,
                providerId = providerId,
                dateTimeStamp = randomId,
                status = "PENDING",
                price = price
            )
            bookingRepository.createBooking(newBooking)
        }
    }

    fun updateStatus(bookingId: String, newStatus: String) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, newStatus)
        }
    }
}