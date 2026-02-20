package com.example.dadada.Repository
import com.example.dadada.Domain.FilmItemModel
import com.google.firebase.database.FirebaseDatabase
import java.security.MessageDigest
import java.util.UUID

data class CheckoutPaymentData(
    val cardNumber: String,
    val cardholderName: String,
    val expMonth: String,
    val expYear: String,
    val cvv: String,
    val billingZip: String
)

data class CheckoutResult(
    val success: Boolean,
    val message: String,
    val orderId: String? = null
)

class CheckoutRepository(
) {
    private companion object {
        private const val HEX_CHARS = "0123456789abcdef"
    }

    private val firebaseDatabase = FirebaseDatabase.getInstance(
        "https://cs330-pz-6310-default-rtdb.europe-west1.firebasedatabase.app/"
    )

    fun submitOrder(
        username: String,
        userScope: String,
        cartItems: List<FilmItemModel>,
        paymentData: CheckoutPaymentData,
        onResult: (CheckoutResult) -> Unit
    ) {
        if (cartItems.isEmpty()) {
            onResult(CheckoutResult(false, "Cart is empty"))
            return
        }

        val usernameKey = usernameToKey(userScope)
        val orderId = firebaseDatabase.reference
            .child("OrdersByUser")
            .child(usernameKey)
            .push()
            .key
        if (orderId.isNullOrBlank()) {
            onResult(CheckoutResult(false, "Failed to create order id"))
            return
        }

        val normalizedCardNumber = paymentData.cardNumber.filter { it.isDigit() }
        val tokenizedCard = tokenizePaymentMethod(normalizedCardNumber)

        val now = System.currentTimeMillis()
        val totalAmount = cartItems.sumOf { it.price }

        val orderItems = cartItems.map { item ->
            mapOf(
                "title" to item.Title,
                "year" to item.Year,
                "poster" to item.Poster,
                "price" to item.price,
                "imdb" to item.Imdb
            )
        }

        val orderData = mapOf(
            "id" to orderId,
            "username" to username,
            "usernameKey" to usernameKey,
            "status" to "PENDING",
            "createdAt" to now,
            "totalAmount" to totalAmount,
            "items" to orderItems
        )

        // CVV and full PAN are intentionally never persisted by the app.
        val paymentRecord = mapOf(
            "id" to orderId,
            "orderId" to orderId,
            "username" to username,
            "usernameKey" to usernameKey,
            "provider" to "tokenized_placeholder",
            "paymentToken" to tokenizedCard.token,
            "last4" to tokenizedCard.last4,
            "brand" to tokenizedCard.brand,
            "cardholderName" to paymentData.cardholderName.trim(),
            "expMonth" to paymentData.expMonth,
            "expYear" to paymentData.expYear,
            "createdAt" to now
        )

        val updates = hashMapOf<String, Any>(
            "/OrdersByUser/$usernameKey/$orderId" to orderData,
            "/PaymentTokensByUser/$usernameKey/$orderId" to paymentRecord
        )

        firebaseDatabase.reference.updateChildren(updates)
            .addOnSuccessListener {
                onResult(
                    CheckoutResult(
                        success = true,
                        message = "Order placed successfully",
                        orderId = orderId
                    )
                )
            }
            .addOnFailureListener { error ->
                onResult(
                    CheckoutResult(
                        success = false,
                        message = error.message ?: "Checkout failed"
                    )
                )
            }
    }

    private fun tokenizePaymentMethod(cardNumber: String): TokenizedCard {
        return TokenizedCard(
            token = "tok_${UUID.randomUUID()}",
            last4 = cardNumber.takeLast(4),
            brand = detectCardBrand(cardNumber)
        )
    }

    private fun detectCardBrand(cardNumber: String): String {
        return when {
            cardNumber.startsWith("4") -> "VISA"
            cardNumber.startsWith("5") -> "MASTERCARD"
            cardNumber.startsWith("34") || cardNumber.startsWith("37") -> "AMEX"
            cardNumber.startsWith("6") -> "DISCOVER"
            else -> "UNKNOWN"
        }
    }

    private fun usernameToKey(scope: String): String {
        val normalized = scope.trim()
        if (normalized.isBlank()) return "guest"

        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
        val out = StringBuilder(digest.size * 2)
        for (byte in digest) {
            val intValue = byte.toInt() and 0xFF
            out.append(HEX_CHARS[intValue ushr 4])
            out.append(HEX_CHARS[intValue and 0x0F])
        }
        return out.toString()
    }
}

private data class TokenizedCard(
    val token: String,
    val last4: String,
    val brand: String
)
