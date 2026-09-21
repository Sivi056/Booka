package com.example.booka.data.repository

import com.example.booka.domain.model.Booking
import com.example.booka.domain.repository.BookingRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.serializer

class BookingRepositoryImpl : BookingRepository {

    private val firestore = Firebase.firestore
    private val bookingsCollection = firestore.collection("bookings")

    override fun getBookings(): Flow<List<Booking>> {
        return bookingsCollection.snapshots.map { snapshot ->
            snapshot.documents.map { doc ->
                // explicitly map to the Booking serializer
                doc.data(Booking.serializer())
            }
        }
    }

    override suspend fun createBooking(booking: Booking) {
        // Explicitly pass the strategy and the data
        bookingsCollection.add(Booking.serializer(), booking)
    }
}