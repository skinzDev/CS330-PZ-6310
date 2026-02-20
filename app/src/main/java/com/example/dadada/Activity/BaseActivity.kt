package com.example.dadada.Activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onStart() {
        super.onStart()
        if (!requiresAuthentication()) return
        if (FirebaseAuth.getInstance().currentUser != null) return

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    protected open fun requiresAuthentication(): Boolean = true

    protected fun openBottomNavDestination(index: Int, currentIndex: Int) {
        if (index == currentIndex) return

        val destination = when (index) {
            0 -> MainActivity::class.java
            1 -> MyListActivity::class.java
            2 -> CartActivity::class.java
            3 -> ProfileActivity::class.java
            else -> null
        } ?: return

        startActivity(Intent(this, destination))
        finish()
    }
}
