package com.example.calculadoraimc3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvSwitchToRegister: TextView
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Verificar si ya hay un usuario autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            } else {
                // Usuario no verificado, ir a pantalla de verificación
                val intent = Intent(this, EmailVerificationActivity::class.java)
                intent.putExtra("email", currentUser.email)
                intent.putExtra("username", currentUser.displayName)
                startActivity(intent)
                finish()
                return
            }
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        tvSwitchToRegister = findViewById(R.id.tvSwitchToRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            loginUser()
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvSwitchToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user?.isEmailVerified == true) {
                        Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, getString(R.string.please_verify_email), Toast.LENGTH_LONG).show()

                        // Ir a la pantalla de verificación
                        val intent = Intent(this, EmailVerificationActivity::class.java)
                        intent.putExtra("email", user?.email)
                        intent.putExtra("username", user?.displayName)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.login_failed) + ": ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showForgotPasswordDialog() {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_email_reset), Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.password_reset_sent), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, getString(R.string.password_reset_failed) + ": ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }
}