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
import com.example.booka.presentation.settings.SettingsScreen
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ServiceItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val providerId: String = "",
    val timeSlots: List<String> = listOf("Tomorrow at 10:00 AM", "Tomorrow at 02:00 PM")
)

@Serializable
data class Booking(
    val id: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val clientId: String = "",
    val clientEmail: String = "",
    val providerId: String = "",
    val timeSlot: String = "",
    val price: Double = 0.0,
    val status: String = "Pending"
)

@Composable
fun App() {
    MaterialTheme {
        val authRepository = remember { AuthRepositoryImpl() }
        val authViewModel = remember { AuthViewModel(authRepository) }
        val scope = rememberCoroutineScope()

        val currentUser by authRepository.currentUser.collectAsState(initial = null)
        var showSettings by remember { mutableStateOf(false) }

        if (currentUser == null) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { }
            )
        } else {
            if (showSettings) {
                SettingsScreen(
                    onBackClick = { showSettings = false }
                )
            } else {
                MainDashboardScreen(
                    user = currentUser!!,
                    onOpenSettings = { showSettings = true },
                    onSignOut = {
                        scope.launch {
                            authRepository.signOut()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MainDashboardScreen(
    user: User,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    val role = user.role.lowercase().trim()
    if (role.contains("provider")) {
        ProviderDashboardScreen(user = user, onOpenSettings = onOpenSettings, onSignOut = onSignOut)
    } else {
        ClientDashboardScreen(user = user, onOpenSettings = onOpenSettings, onSignOut = onSignOut)
    }
}

@Composable
fun ClientDashboardScreen(
    user: User,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val firestore = remember { Firebase.firestore }

    var services by remember { mutableStateOf<List<ServiceItem>>(emptyList()) }
    var selectedServiceForBooking by remember { mutableStateOf<ServiceItem?>(null) }
    var selectedTimeSlot by remember { mutableStateOf("") }
    var bookingSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Load dynamic services from Firestore
    LaunchedEffect(Unit) {
        try {
            val snapshot = firestore.collection("services").get()
            services = snapshot.documents.map { doc ->
                ServiceItem(
                    id = doc.id,
                    title = doc.get<String?>("title") ?: "",
                    description = doc.get<String?>("description") ?: "",
                    price = doc.get<Double?>("price") ?: 0.0,
                    providerId = doc.get<String?>("providerId") ?: "",
                    timeSlots = doc.get<List<String>?>("timeSlots") ?: listOf("Tomorrow at 10:00 AM", "Tomorrow at 02:00 PM")
                )
            }
        } catch (_: Exception) {}
    }

    selectedServiceForBooking?.let { service ->
        if (selectedTimeSlot.isEmpty() && service.timeSlots.isNotEmpty()) {
            selectedTimeSlot = service.timeSlots.first()
        }
        AlertDialog(
            onDismissRequest = { selectedServiceForBooking = null },
            title = { Text("Select Time Slot") },
            text = {
                Column {
                    Text("Service: ${service.title}")
                    Text("Price: R${service.price}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Available Slots:", style = MaterialTheme.typography.labelLarge)
                    service.timeSlots.forEach { slot ->
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
                                    "serviceId" to service.id,
                                    "serviceTitle" to service.title,
                                    "clientId" to user.uid,
                                    "clientEmail" to user.email,
                                    "providerId" to service.providerId,
                                    "timeSlot" to selectedTimeSlot,
                                    "price" to service.price,
                                    "status" to "Pending"
                                )
                                firestore.collection("bookings").add(bookingData)
                                bookingSuccessMessage = "Successfully booked ${service.title} for $selectedTimeSlot!"
                                selectedServiceForBooking = null
                            } catch (e: Exception) {
                                bookingSuccessMessage = "Booking failed: ${e.message}"
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Welcome, ${user.displayName.ifEmpty { "Client" }}!", style = MaterialTheme.typography.headlineSmall)
                Text("Browse and book available services", style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                OutlinedButton(onClick = onOpenSettings) { Text("Settings") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSignOut) { Text("Sign Out") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        bookingSuccessMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(text = msg, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
        }

        Text("Available Services", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (services.isEmpty()) {
            Text("No services listed by providers yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(services) { service ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(service.title, style = MaterialTheme.typography.titleMedium)
                            Text(service.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Price: R${service.price}", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { selectedServiceForBooking = service }) {
                                Text("Book Service")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderDashboardScreen(
    user: User,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val firestore = remember { Firebase.firestore }

    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var showAddServiceDialog by remember { mutableStateOf(false) }

    // Form fields for adding a service
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newTimeSlot by remember { mutableStateOf("Tomorrow at 11:00 AM") }

    LaunchedEffect(Unit) {
        firestore.collection("bookings").snapshots.collect { snapshot ->
            bookings = snapshot.documents.map { doc ->
                Booking(
                    id = doc.id,
                    serviceTitle = doc.get<String?>("serviceTitle") ?: "",
                    clientEmail = doc.get<String?>("clientEmail") ?: "",
                    timeSlot = doc.get<String?>("timeSlot") ?: "",
                    status = doc.get<String?>("status") ?: "Pending",
                    providerId = doc.get<String?>("providerId") ?: ""
                )
            }.filter { it.providerId == user.uid || it.providerId.isEmpty() }
        }
    }

    if (showAddServiceDialog) {
        AlertDialog(
            onDismissRequest = { showAddServiceDialog = false },
            title = { Text("Add New Service Offering") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Service Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("Price (R)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTimeSlot,
                        onValueChange = { newTimeSlot = it },
                        label = { Text("Available Time Slot") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val priceVal = newPrice.toDoubleOrNull() ?: 0.0
                    if (newTitle.isNotBlank() && priceVal > 0.0) {
                        scope.launch {
                            val serviceMap = mapOf(
                                "title" to newTitle,
                                "description" to newDescription,
                                "price" to priceVal,
                                "providerId" to user.uid,
                                "timeSlots" to listOf(newTimeSlot, "Tomorrow at 03:00 PM")
                            )
                            firestore.collection("services").add(serviceMap)
                            newTitle = ""
                            newDescription = ""
                            newPrice = ""
                            showAddServiceDialog = false
                        }
                    }
                }) {
                    Text("Save Service")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddServiceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Provider Portal", style = MaterialTheme.typography.headlineSmall)
                Text("Manage services and bookings", style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                OutlinedButton(onClick = onOpenSettings) { Text("Settings") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSignOut) { Text("Sign Out") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showAddServiceDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add New Service & Time Slots")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Incoming Requests (${bookings.size})", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (bookings.isEmpty()) {
            Text("No booking requests received yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bookings) { booking ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Client: ${booking.clientEmail}", style = MaterialTheme.typography.titleMedium)
                            Text("Service: ${booking.serviceTitle}", style = MaterialTheme.typography.bodyMedium)
                            Text("Time Slot: ${booking.timeSlot}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Status: ${booking.status}", style = MaterialTheme.typography.labelLarge)
                                if (booking.status != "Confirmed") {
                                    Button(onClick = {
                                        scope.launch {
                                            firestore.collection("bookings").document(booking.id).update("status" to "Confirmed")
                                        }
                                    }) {
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