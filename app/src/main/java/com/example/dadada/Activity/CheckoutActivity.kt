package com.example.dadada.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.dadada.BottomNavigationBar
import com.example.dadada.Domain.FilmItemModel
import com.example.dadada.R
import com.example.dadada.Repository.CartRepository
import com.example.dadada.Repository.CheckoutPaymentData
import com.example.dadada.Repository.CheckoutRepository
import com.example.dadada.ui.theme.DadadaTheme
import android.content.Context
import java.util.Calendar

class CheckoutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DadadaTheme {
                CheckoutScreen(
                    onBottomNavClick = { index -> openBottomNavDestination(index, currentIndex = 2) }
                )
            }
        }
    }
}

@Composable
private fun CheckoutScreen(onBottomNavClick: (Int) -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(LoginActivity.PREFS_AUTH, Context.MODE_PRIVATE)
    val username = prefs
        .getString(LoginActivity.KEY_USERNAME, "")
        .orEmpty()
    val userScope = AuthSessionScope.resolveUserScope(context)
    val cartRepository = remember(context, userScope) { CartRepository(context, userScope) }
    val checkoutRepository = remember { CheckoutRepository() }
    val cartItems = remember { mutableStateListOf<FilmItemModel>() }

    var cardNumber by rememberSaveable { mutableStateOf("") }
    var cardholderName by rememberSaveable { mutableStateOf("") }
    var expMonth by rememberSaveable { mutableStateOf("") }
    var expYear by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }
    var billingZip by rememberSaveable { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(userScope) {
        cartItems.clear()
        cartItems.addAll(cartRepository.getCartItems())
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                initialSelectedItem = 2,
                onItemSelected = { _, index -> onBottomNavClick(index) }
            )
        },
        containerColor = colorResource(R.color.blackBackground)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.blackBackground))
        ) {
            Image(
                painter = painterResource(R.drawable.bg1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                alpha = 0.25f
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Checkout",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                if (cartItems.isEmpty()) {
                    Text(
                        text = "Your cart is empty.",
                        color = Color.LightGray
                    )
                    return@Column
                }

                Text(
                    text = "Paying securely for ${cartItems.size} item(s)",
                    color = Color.LightGray
                )
                Text(
                    text = "CVV is used for payment validation only and is never stored.",
                    color = Color(0xFF8CFF98)
                )

                CheckoutField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    label = "Cardholder Name"
                )
                CheckoutField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it.filter(Char::isDigit).take(19) },
                    label = "Card Number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                CheckoutField(
                    value = expMonth,
                    onValueChange = { expMonth = it.filter(Char::isDigit).take(2) },
                    label = "Expiry Month (MM)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                CheckoutField(
                    value = expYear,
                    onValueChange = { expYear = it.filter(Char::isDigit).take(4) },
                    label = "Expiry Year (YYYY)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                CheckoutField(
                    value = cvv,
                    onValueChange = { cvv = it.filter(Char::isDigit).take(4) },
                    label = "CVV",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isSecret = true
                )
                CheckoutField(
                    value = billingZip,
                    onValueChange = { billingZip = it.filter(Char::isDigit).take(10) },
                    label = "Billing ZIP/Postal Code",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (statusMessage.isNotBlank()) {
                    Text(
                        text = statusMessage,
                        color = if (isError) Color(0xFFFF8A80) else Color(0xFF8CFF98)
                    )
                }

                Button(
                    onClick = {
                        val validationError = validatePaymentData(
                            cardNumber = cardNumber,
                            cardholderName = cardholderName,
                            expMonth = expMonth,
                            expYear = expYear,
                            cvv = cvv,
                            billingZip = billingZip
                        )

                        if (validationError != null) {
                            isError = true
                            statusMessage = validationError
                            return@Button
                        }

                        isSubmitting = true
                        isError = false
                        statusMessage = ""

                        checkoutRepository.submitOrder(
                            username = username.ifBlank { "guest" },
                            userScope = userScope.ifBlank { username.ifBlank { "guest" } },
                            cartItems = cartItems,
                            paymentData = CheckoutPaymentData(
                                cardNumber = cardNumber,
                                cardholderName = cardholderName.trim(),
                                expMonth = expMonth,
                                expYear = expYear,
                                cvv = cvv,
                                billingZip = billingZip
                            )
                        ) { result ->
                            isSubmitting = false
                            isError = !result.success
                            statusMessage = result.message
                            if (result.success) {
                                cartRepository.clearCart()
                                cartItems.clear()
                                cardNumber = ""
                                cvv = ""
                                Toast.makeText(
                                    context,
                                    "Checkout completed. Order: ${result.orderId ?: "N/A"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.pink),
                        contentColor = Color.White
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.height(22.dp)
                        )
                    } else {
                        Text(text = "Place Secure Order")
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun CheckoutField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isSecret: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = keyboardOptions,
        visualTransformation = if (isSecret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.LightGray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.LightGray
        )
    )
}

internal fun validatePaymentData(
    cardNumber: String,
    cardholderName: String,
    expMonth: String,
    expYear: String,
    cvv: String,
    billingZip: String,
    currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
): String? {
    val normalizedCard = cardNumber.filter(Char::isDigit)
    if (cardholderName.trim().length < 2) return "Cardholder name is required"
    if (normalizedCard.length !in 13..19) return "Card number must be 13-19 digits"
    if (!isValidLuhn(normalizedCard)) return "Card number is invalid"

    val month = expMonth.toIntOrNull() ?: return "Invalid expiry month"
    if (month !in 1..12) return "Expiry month must be between 01 and 12"

    val year = expYear.toIntOrNull() ?: return "Invalid expiry year"
    if (year < currentYear || year > currentYear + 20) return "Expiry year is invalid"
    if (year == currentYear && month < currentMonth) return "Card is expired"

    if (cvv.length !in 3..4) return "CVV must be 3 or 4 digits"
    if (billingZip.length < 3) return "Billing ZIP/Postal Code is too short"

    return null
}

internal fun isValidLuhn(number: String): Boolean {
    var sum = 0
    var shouldDouble = false
    for (index in number.length - 1 downTo 0) {
        var digit = number[index].digitToIntOrNull() ?: return false
        if (shouldDouble) {
            digit *= 2
            if (digit > 9) digit -= 9
        }
        sum += digit
        shouldDouble = !shouldDouble
    }
    return sum % 10 == 0
}
