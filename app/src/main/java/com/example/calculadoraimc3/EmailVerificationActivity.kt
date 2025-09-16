package com.example.calculadoraimc3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvVerificationMessage: TextView
    private lateinit var btnCheckVerification: Button
    private lateinit var btnResendEmail: Button
    private lateinit var btnBackToLogin: Button
    private var email: String? = null
    private var username: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var checkVerificationRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        auth = FirebaseAuth.getInstance()

        // Obtener datos del intent
        email = intent.getStringExtra("email")
        username = intent.getStringExtra("username")

        initViews()
        setupClickListeners()
        updateUI()

        // Comenzar verificación automática
        startAutoCheck()
    }

    private fun initViews() {
        tvVerificationMessage = findViewById(R.id.tvVerificationMessage)
        btnCheckVerification = findViewById(R.id.btnCheckVerification)
        btnResendEmail = findViewById(R.id.btnResendEmail)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)
    }

    private fun setupClickListeners() {
        btnCheckVerification.setOnClickListener {
            checkEmailVerification()
        }

        btnResendEmail.setOnClickListener {
            resendVerificationEmail()
        }

        btnBackToLogin.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun updateUI() {
        val message = getString(R.string.verification_message, email ?: "")
        tvVerificationMessage.text = message
    }

    private fun checkEmailVerification() {
        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    if (user.isEmailVerified) {
                        Toast.makeText(this, getString(R.string.email_verified_success), Toast.LENGTH_SHORT).show()

                        // Ir a la actividad principal
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, getString(R.string.email_not_verified_yet), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.error_checking_verification), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resendVerificationEmail() {
        val user = auth.currentUser
        if (user != null) {
            btnResendEmail.isEnabled = false
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.verification_email_resent), Toast.LENGTH_SHORT).show()

                        // Deshabilitar el botón por 60 segundos
                        handler.postDelayed({
                            btnResendEmail.isEnabled = true
                        }, 60000)
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.failed_resend_verification) + ": ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        btnResendEmail.isEnabled = true
                    }
                }
        }
    }

    private fun startAutoCheck() {
        checkVerificationRunnable = object : Runnable {
            override fun run() {
                val user = auth.currentUser
                if (user != null) {
                    user.reload().addOnCompleteListener { reloadTask ->
                        if (reloadTask.isSuccessful && user.isEmailVerified) {
                            // Email verificado, ir a MainActivity
                            Toast.makeText(this@EmailVerificationActivity,
                                getString(R.string.email_verified_success), Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@EmailVerificationActivity, MainActivity::class.java))
                            finish()
                        } else {
                            // Seguir verificando cada 3 segundos
                            handler.postDelayed(this, 3000)
                        }
                    }
                }
            }
        }
        handler.postDelayed(checkVerificationRunnable!!, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        checkVerificationRunnable?.let { handler.removeCallbacks(it) }
    }
}