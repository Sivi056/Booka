package com.example.booka.domain.repository

import com.example.booka.domain.model.Booking
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun getBookings(): Flow<List<Booking>>
    suspend fun createBooking(booking: Booking)
}