package com.example.dadada.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dadada.R
import com.example.dadada.Repository.AuthRepository
import com.example.dadada.ui.theme.DadadaTheme
import java.util.Locale

class LoginActivity : BaseActivity() {
    private val authRepository = AuthRepository()

    override fun requiresAuthentication(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DadadaTheme {
                LoginScreen(onSubmit = { isRegister, username, password, onResult ->
                    if (isRegister) {
                        authRepository.register(username, password, onResult)
                    } else {
                        authRepository.login(username, password, onResult)
                    }
                }, onAuthSuccess = { username, userId ->
                    getSharedPreferences(PREFS_AUTH, MODE_PRIVATE)
                        .edit()
                        .putString(KEY_USERNAME, username)
                        .putString(KEY_USER_SCOPE, resolveUserScope(username, userId))
                        .apply()

                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                })
            }
        }
    }

    companion object {
        const val PREFS_AUTH = "auth_prefs"
        const val KEY_USERNAME = "username"
        const val KEY_USER_SCOPE = "user_scope"
    }

    private fun resolveUserScope(username: String, userId: String?): String {
        val id = userId?.trim().orEmpty()
        if (id.isNotBlank()) return "id_$id"
        val normalized = username
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9_]"), "_")
        return "name_${normalized.ifBlank { "guest" }}"
    }
}

@Composable
@Preview
fun LoginScreenPreview(){
    LoginScreen(
        onSubmit = { _, _, _, callback ->
            callback(AuthRepository.AuthResult(false, "Preview mode"))
        },
        onAuthSuccess = { _, _ -> }
    )
}

@Composable
fun LoginScreen(
    onSubmit: (
        isRegister: Boolean,
        username: String,
        password: String,
        onResult: (AuthRepository.AuthResult) -> Unit
    ) -> Unit,
    onAuthSuccess: (String, String?) -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.blackBackground))
    ) {
        Image(
            painter = painterResource(R.drawable.bg1),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(128.dp))
            Text(
                text = if (isRegisterMode) "Register" else "Log In",
                style = TextStyle (
                    color = Color.White,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(128.dp))
            GradientTextField(
                value = username,
                onValueChange = { username = it },
                hint = "Username",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            GradientTextField(
                value = password,
                onValueChange = { password = it },
                hint = "Password",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default,
                isPassword = true
            )
            if (isRegisterMode) {
                Spacer(modifier = Modifier.height(16.dp))
                GradientTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    hint = "Confirm Password",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword = true
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text (
                text = if (isRegisterMode) "Already have an account? Log In" else "Don't have an account? Register",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isRegisterMode = !isRegisterMode
                        statusMessage = ""
                        isError = false
                        confirmPassword = ""
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = if (isLoading) "Please wait..." else if (isRegisterMode) "Register" else "Login",
                enabled = !isLoading,
                onClick = {
                    val normalizedUsername = username.trim()
                    if (normalizedUsername.length < 3) {
                        isError = true
                        statusMessage = "Username must be at least 3 characters"
                        return@GradientButton
                    }
                    if (password.length < 6) {
                        isError = true
                        statusMessage = "Password must be at least 6 characters"
                        return@GradientButton
                    }
                    if (isRegisterMode && password != confirmPassword) {
                        isError = true
                        statusMessage = "Confirm password does not match"
                        return@GradientButton
                    }

                    isLoading = true
                    statusMessage = ""
                    onSubmit(isRegisterMode, normalizedUsername, password) { result ->
                        isLoading = false
                        isError = !result.success
                        statusMessage = result.message
                        if (result.success) {
                            onAuthSuccess(normalizedUsername, result.userId)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            if (statusMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = statusMessage,
                    color = if (isError) Color(0xFFFF6B6B) else Color(0xFF8CFF98),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    enabled: Boolean,
    onClick:() -> Unit,
    modifier: Modifier= Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(60.dp),
        border = BorderStroke(
            width = 4.dp,
            brush = Brush.linearGradient(
                colors = listOf(colorResource(R.color.pink), colorResource(R.color.green))
            )
        ),
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color.Transparent
        )
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GradientTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier:Modifier= Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPassword: Boolean = false
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colorResource(R.color.pink), colorResource(R.color.green))
                ),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = hint,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                textAlign = TextAlign.Center
            ),
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,
            ),
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.black1),
                    shape = RoundedCornerShape(50.dp)
                )
                .align(Alignment.Center)
        )
    }

}
