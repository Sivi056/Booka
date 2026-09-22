package com.example.booka.data.repository

import com.example.booka.domain.model.Booking
import com.example.booka.domain.model.ServiceItem
import com.example.booka.domain.repository.BookingRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookingRepositoryImpl : BookingRepository {

    private val firestore = Firebase.firestore

    override fun getAvailableServices(): Flow<List<ServiceItem>> {
        return firestore.collection("services")
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data(ServiceItem.serializer())
                }
            }
    }

    override fun getBookingsForUser(userId: String, isProvider: Boolean): Flow<List<Booking>> {
        val fieldName = if (isProvider) "providerId" else "clientId"
        return firestore.collection("bookings")
            .where(fieldName, equalTo = userId)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data(Booking.serializer())
                }
            }
    }

    override suspend fun createBooking(booking: Booking): Result<Unit> {
        return try {
            firestore.collection("bookings")
                .document(booking.id)
                .set(Booking.serializer(), booking)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, newStatus: String): Result<Unit> {
        return try {
            firestore.collection("bookings")
                .document(bookingId)
                .update("status" to newStatus)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}