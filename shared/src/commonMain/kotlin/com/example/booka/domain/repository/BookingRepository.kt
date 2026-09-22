package com.example.booka.domain.repository

import com.example.booka.domain.model.Booking
import com.example.booka.domain.model.ServiceItem
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun getAvailableServices(): Flow<List<ServiceItem>>
    fun getBookingsForUser(userId: String, isProvider: Boolean): Flow<List<Booking>>
    suspend fun createBooking(booking: Booking): Result<Unit>
    suspend fun updateBookingStatus(bookingId: String, newStatus: String): Result<Unit>
}