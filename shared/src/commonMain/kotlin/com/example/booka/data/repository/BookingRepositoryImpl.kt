package com.example.booka.data.repository

import com.example.booka.domain.model.Booking
import com.example.booka.domain.model.ServiceItem
import com.example.booka.domain.repository.BookingRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BookingRepositoryImpl : BookingRepository {
    private val db = Firebase.firestore

    override fun getAvailableServices(): Flow<List<ServiceItem>> = flow {
        try {
            val snapshot = db.collection("services").get()
            var services = snapshot.documents.map { doc ->
                val data = doc.data<Map<String, Any>>()
                ServiceItem(
                    id = doc.id,
                    title = data["title"]?.toString() ?: "",
                    description = data["description"]?.toString() ?: "",
                    price = data["price"]?.toString()?.toDoubleOrNull() ?: 0.0,
                    providerId = data["providerId"]?.toString() ?: ""
                )
            }

            if (services.isEmpty()) {
                seedInitialServices()
                val newSnapshot = db.collection("services").get()
                services = newSnapshot.documents.map { doc ->
                    val data = doc.data<Map<String, Any>>()
                    ServiceItem(
                        id = doc.id,
                        title = data["title"]?.toString() ?: "",
                        description = data["description"]?.toString() ?: "",
                        price = data["price"]?.toString()?.toDoubleOrNull() ?: 0.0,
                        providerId = data["providerId"]?.toString() ?: ""
                    )
                }
            }
            emit(services)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getBookingsForUser(userId: String, isProvider: Boolean): Flow<List<Booking>> = flow {
        try {
            val fieldName = if (isProvider) "providerId" else "clientId"
            val snapshot = db.collection("bookings").get()

            val bookings = snapshot.documents
                .map { doc ->
                    val data = doc.data<Map<String, Any>>()
                    Booking(
                        id = doc.id,
                        serviceId = data["serviceId"]?.toString() ?: "",
                        serviceTitle = data["serviceTitle"]?.toString() ?: "",
                        clientId = data["clientId"]?.toString() ?: "",
                        providerId = data["providerId"]?.toString() ?: "",
                        price = data["price"]?.toString()?.toDoubleOrNull() ?: 0.0,
                        status = data["status"]?.toString() ?: "PENDING"
                    )
                }
                .filter { booking ->
                    if (isProvider) booking.providerId == userId else booking.clientId == userId
                }

            emit(bookings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun createBooking(booking: Booking): Result<Unit> {
        return try {
            db.collection("bookings").add(
                mapOf(
                    "serviceId" to booking.serviceId,
                    "serviceTitle" to booking.serviceTitle,
                    "clientId" to booking.clientId,
                    "providerId" to booking.providerId,
                    "price" to booking.price,
                    "status" to booking.status
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, newStatus: String): Result<Unit> {
        return try {
            db.collection("bookings").document(bookingId).update(mapOf("status" to newStatus))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun seedInitialServices() {
        val initialServices = listOf(
            mapOf("title" to "Hair Styling & Braiding", "description" to "Full wash, blow dry, and custom braids.", "price" to 350.0, "providerId" to "provider1"),
            mapOf("title" to "Graphic Poster Design", "description" to "Custom high-res promotional event poster.", "price" to 200.0, "providerId" to "provider2"),
            mapOf("title" to "Mobile App Consultation", "description" to "1-on-1 session on architecture and UI.", "price" to 500.0, "providerId" to "provider1")
        )
        for (service in initialServices) {
            db.collection("services").add(service)
        }
    }
}