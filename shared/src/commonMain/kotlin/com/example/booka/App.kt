package com.example.booka

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booka.data.repository.AuthRepositoryImpl
import com.example.booka.domain.model.User
import com.example.booka.presentation.auth.AuthScreen
import com.example.booka.presentation.auth.AuthViewModel

@Composable
fun App() {
    MaterialTheme {
        val authRepository = remember { AuthRepositoryImpl() }
        val authViewModel = remember { AuthViewModel(authRepository) }

        val currentUser by authRepository.currentUser.collectAsState(initial = null)

        if (currentUser == null) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    // State automatically updates via currentUser state flow
                }
            )
        } else {
            MainDashboardScreen(user = currentUser!!)
        }
    }
}

@Composable
fun MainDashboardScreen(user: User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Booka, ${user.displayName}!")
        Text("Role: ${user.role}")
    }
}