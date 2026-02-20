package com.example.dadada.Activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckoutValidationTest {

    @Test
    fun `accepts valid card for current month`() {
        val error = validatePaymentData(
            cardNumber = "4242424242424242",
            cardholderName = "Test User",
            expMonth = "02",
            expYear = "2026",
            cvv = "123",
            billingZip = "10001",
            currentYear = 2026,
            currentMonth = 2
        )

        assertNull(error)
    }

    @Test
    fun `rejects expired card in same year`() {
        val error = validatePaymentData(
            cardNumber = "4242424242424242",
            cardholderName = "Test User",
            expMonth = "01",
            expYear = "2026",
            cvv = "123",
            billingZip = "10001",
            currentYear = 2026,
            currentMonth = 2
        )

        assertEquals("Card is expired", error)
    }

    @Test
    fun `rejects invalid luhn card number`() {
        val error = validatePaymentData(
            cardNumber = "4242424242424241",
            cardholderName = "Test User",
            expMonth = "12",
            expYear = "2030",
            cvv = "123",
            billingZip = "10001",
            currentYear = 2026,
            currentMonth = 2
        )

        assertEquals("Card number is invalid", error)
    }
}
