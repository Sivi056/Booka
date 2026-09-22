package com.example.booka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booka.data.repository.AuthRepositoryImpl
import com.example.booka.domain.model.User
import com.example.booka.presentation.auth.AuthScreen
import com.example.booka.presentation.auth.AuthViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Data model matching Firestore documents
@Serializable
data class Booking(
    val id: String = "",
    val serviceName: String = "",
    val clientEmail: String = "",
    val timeSlot: String = "",
    val status: String = "Pending"
)

@Composable
fun App() {
    MaterialTheme {
        val authRepository = remember { AuthRepositoryImpl() }
        val authViewModel = remember { AuthViewModel(authRepository) }
        val scope = rememberCoroutineScope()

        val currentUser by authRepository.currentUser.collectAsState(initial = null)

        if (currentUser == null) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { }
            )
        } else {
            MainDashboardScreen(
                user = currentUser!!,
                onSignOut = {
                    scope.launch {
                        authRepository.signOut()
                    }
                }
            )
        }
    }
}

@Composable
fun MainDashboardScreen(user: User, onSignOut: () -> Unit) {
    val role = user.role.lowercase().trim()
    if (role.contains("provider")) {
        ProviderDashboardScreen(user = user, onSignOut = onSignOut)
    } else {
        ClientDashboardScreen(user = user, onSignOut = onSignOut)
    }
}

@Composable
fun ClientDashboardScreen(user: User, onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    val firestore = remember { Firebase.firestore }

    var selectedServiceForBooking by remember { mutableStateOf<String?>(null) }
    var selectedTimeSlot by remember { mutableStateOf("Tomorrow at 10:00 AM") }
    var bookingSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val timeSlots = listOf("Tomorrow at 10:00 AM", "Tomorrow at 02:00 PM", "Tomorrow at 04:30 PM")
    val services = listOf(
        "Hair Styling & Braiding - R250",
        "Nail Art & Manicure - R180",
        "Full Face Makeup - R300",
        "Photography Session - R500"
    )

    // Booking Dialog
    if (selectedServiceForBooking != null) {
        AlertDialog(
            onDismissRequest = { selectedServiceForBooking = null },
            title = { Text("Select Time Slot") },
            text = {
                Column {
                    Text("Service: ${selectedServiceForBooking!!}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Available Slots:", style = MaterialTheme.typography.labelLarge)
                    timeSlots.forEach { slot ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = (selectedTimeSlot == slot),
                                onClick = { selectedTimeSlot = slot }
                            )
                            Text(slot, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSubmitting,
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            try {
                                val bookingData = mapOf(
                                    "serviceName" to selectedServiceForBooking,
                                    "clientEmail" to user.email,
                                    "timeSlot" to selectedTimeSlot,
                                    "status" to "Pending"
                                )
                                firestore.collection("bookings").add(bookingData)
                                bookingSuccessMessage = "Booked ${selectedServiceForBooking!!} for $selectedTimeSlot"
                                selectedServiceForBooking = null
                            } catch (e: Exception) {
                                bookingSuccessMessage = "Failed to create booking: ${e.message}"
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                ) {
                    Text(if (isSubmitting) "Booking..." else "Confirm Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedServiceForBooking = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Welcome, ${user.displayName.ifEmpty { "Client" }}!", style = MaterialTheme.typography.headlineSmall)
                Text("Explore local services", style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onSignOut) {
                Text("Sign Out")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        bookingSuccessMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Text("Available Services", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(services) { service ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(service, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Button(onClick = { selectedServiceForBooking = service }) {
                            Text("Book")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderDashboardScreen(user: User, onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    val firestore = remember { Firebase.firestore }
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }

    // Fetch real-time bookings from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("bookings").snapshots.collect { snapshot ->
            val fetchedBookings = snapshot.documents.map { doc ->
                Booking(
                    id = doc.id,
                    serviceName = doc.get<String>("serviceName"),
                    clientEmail = doc.get<String>("clientEmail"),
                    timeSlot = doc.get<String>("timeSlot"),
                    status = doc.get<String>("status")
                )
            }
            bookings = fetchedBookings
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Provider Portal", style = MaterialTheme.typography.headlineSmall)
                Text("Manage incoming bookings", style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onSignOut) {
                Text("Sign Out")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Incoming Requests (${bookings.size})", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (bookings.isEmpty()) {
            Text("No booking requests yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bookings) { booking ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Client: ${booking.clientEmail}", style = MaterialTheme.typography.titleMedium)
                            Text("Service: ${booking.serviceName}", style = MaterialTheme.typography.bodyMedium)
                            Text("Time: ${booking.timeSlot}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ${booking.status}",
                                    color = if (booking.status == "Confirmed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                if (booking.status != "Confirmed") {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                firestore.collection("bookings").document(booking.id).update("status" to "Confirmed")
                                            }
                                        }
                                    ) {
                                        Text("Accept")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}