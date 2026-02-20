package com.example.dadada.ui.theme

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.material.Typography as Typography2
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

private val funnelDisplay = GoogleFont("Funnel Display")

fun funnelDisplayFontFamily(context: Context): FontFamily {
    val provider = googleFontsProvider(context) ?: return FontFamily.Default
    return FontFamily(
        Font(googleFont = funnelDisplay, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = funnelDisplay, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = funnelDisplay, fontProvider = provider, weight = FontWeight.SemiBold),
        Font(googleFont = funnelDisplay, fontProvider = provider, weight = FontWeight.Bold)
    )
}

fun appTypography(fontFamily: FontFamily): Typography {
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        )
    )
}

fun legacyTypography(fontFamily: FontFamily): Typography2 {
    return Typography2(defaultFontFamily = fontFamily)
}

private fun googleFontsProvider(context: Context): GoogleFont.Provider? {
    val certificates = googlePlayServicesCertificates(context) ?: return null
    return GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = certificates
    )
}

private fun googlePlayServicesCertificates(context: Context): List<List<ByteArray>>? {
    return try {
        val packageManager = context.packageManager
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(
                "com.google.android.gms",
                PackageManager.GET_SIGNING_CERTIFICATES
            ).signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(
                "com.google.android.gms",
                PackageManager.GET_SIGNATURES
            ).signatures
        } ?: return null

        if (signatures.isEmpty()) return null
        listOf(signatures.map { it.toByteArray() })
    } catch (_: Exception) {
        null
    }
}
