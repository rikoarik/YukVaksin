package com.yukvaksin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.yukvaksin.ui.auth.LoginActivity

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        Handler().postDelayed({
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

            val intent = if (isLoggedIn) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, SPLASH_TIME_OUT)
    }
}