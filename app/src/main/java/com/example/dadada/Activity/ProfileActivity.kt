package com.example.dadada.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dadada.BottomNavigationBar
import com.example.dadada.R
import com.example.dadada.Repository.AuthRepository
import com.example.dadada.Repository.UserAccount
import com.example.dadada.ui.theme.DadadaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.Scaffold

class ProfileActivity : BaseActivity() {
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(LoginActivity.PREFS_AUTH, MODE_PRIVATE)
        val storedUsername = prefs.getString(LoginActivity.KEY_USERNAME, "").orEmpty().trim()
        if (!authRepository.isAuthenticated()) {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        setContent {
            DadadaTheme {
                ProfileScreen(
                    initialUsername = storedUsername,
                    authRepository = authRepository,
                    onBottomNavClick = { index -> openBottomNavDestination(index, currentIndex = 3) },
                    onLogoutClick = {
                        authRepository.signOut()
                        prefs.edit()
                            .clear()
                            .apply()

                        val intent = Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    initialUsername: String,
    authRepository: AuthRepository,
    onBottomNavClick: (Int) -> Unit,
    onLogoutClick: () -> Unit
) {
    var user by remember { mutableStateOf<UserAccount?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authRepository.getCurrentUser { loadedUser, error ->
            user = loadedUser
            errorMessage = error
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                initialSelectedItem = 3,
                onItemSelected = { _, index -> onBottomNavClick(index) }
            )
        },
        containerColor = colorResource(R.color.blackBackground)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = colorResource(R.color.blackBackground))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Profile",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF8A80)
                        )
                    }

                    val profileUser = user
                    ProfileInfoRow("Username", profileUser?.username ?: initialUsername.ifBlank { "N/A" })
                    ProfileInfoRow("User ID", profileUser?.id?.ifBlank { "N/A" } ?: "N/A")
                    ProfileInfoRow(
                        "Member Since",
                        profileUser?.createdAt?.let(::formatCreatedAt) ?: "N/A"
                    )
                    ProfileInfoRow("Session", "Active")
                }

                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB71C1C),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(text = "Log Out")
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatCreatedAt(timestamp: Long): String {
    if (timestamp <= 0L) return "N/A"
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
