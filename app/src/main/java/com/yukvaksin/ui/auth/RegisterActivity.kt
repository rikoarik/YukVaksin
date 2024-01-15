package com.yukvaksin.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.yukvaksin.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var btLogin: Button
    private lateinit var btMasuk: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        btLogin = findViewById(R.id.btLogin)
        btMasuk = findViewById(R.id.btMasuk)

        btLogin.setOnClickListener {
            registerUser()
        }
        btMasuk.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    private fun registerUser() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val phoneNumber = phoneEditText.text.toString().trim()

        if (username.isEmpty()) {
            usernameEditText.error = "Masukkan username"
            usernameEditText.requestFocus()
            return
        }

        if (email.isEmpty()) {
            emailEditText.error = "Masukkan email"
            emailEditText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Email tidak valid"
            emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Masukkan password"
            passwordEditText.requestFocus()
            return
        }

        if (password.length < 8) {
            passwordEditText.error = "Panjang password minimal 8 karakter"
            passwordEditText.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Konfirmasi password"
            confirmPasswordEditText.requestFocus()
            return
        }

        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Password tidak cocok"
            confirmPasswordEditText.requestFocus()
            return
        }

        if (phoneNumber.isEmpty()) {
            phoneEditText.error = "Masukkan nomor telepon"
            phoneEditText.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""

                    val database = FirebaseDatabase.getInstance()
                    val reference = database.getReference("users").child(userId)

                    val userData = HashMap<String, Any>()
                    userData["username"] = username
                    userData["phoneNumber"] = phoneNumber

                    reference.setValue(userData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this, "Registrasi berhasil",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this, "Gagal menyimpan data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {

                    Toast.makeText(
                        this, "Registrasi gagal. ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}