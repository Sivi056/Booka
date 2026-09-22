package com.example.booka.presentation.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booka.domain.model.Booking
import com.example.booka.domain.model.ServiceItem
import com.example.booka.domain.model.User

@Composable
fun BookingScreen(
    user: User,
    viewModel: BookingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(user.id) {
        viewModel.loadServicesAndBookings(
            userId = user.id,
            isProvider = user.role.equals("provider", ignoreCase = true)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (user.role.equals("provider", ignoreCase = true)) "Incoming Bookings" else "Available Services",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = uiState) {
            is BookingUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is BookingUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is BookingUiState.Success -> {
                if (user.role.equals("provider", ignoreCase = true)) {
                    ProviderBookingList(
                        bookings = state.userBookings,
                        onStatusChange = { bookingId, newStatus ->
                            viewModel.updateStatus(bookingId, newStatus)
                        }
                    )
                } else {
                    ClientServiceList(
                        services = state.services,
                        onBookClicked = { service ->
                            viewModel.bookService(
                                serviceId = service.id,
                                serviceTitle = service.title,
                                clientId = user.id,
                                providerId = service.providerId,
                                price = service.price
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClientServiceList(
    services: List<ServiceItem>,
    onBookClicked: (ServiceItem) -> Unit
) {
    if (services.isEmpty()) {
        Text("No services available right now.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(services) { service ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = service.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = service.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "R${service.price}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Button(onClick = { onBookClicked(service) }) {
                                Text("Book Now")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderBookingList(
    bookings: List<Booking>,
    onStatusChange: (String, String) -> Unit
) {
    if (bookings.isEmpty()) {
        Text("No active booking requests.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(bookings) { booking ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = booking.serviceTitle, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Status: ${booking.status}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onStatusChange(booking.id, "CONFIRMED") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Confirm")
                            }
                            OutlinedButton(
                                onClick = { onStatusChange(booking.id, "CANCELLED") }
                            ) {
                                Text("Decline")
                            }
                        }
                    }
                }
            }
        }
    }
}