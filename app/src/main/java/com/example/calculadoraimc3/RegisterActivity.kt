package com.example.calculadoraimc3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseHelper
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvSwitchToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = DatabaseHelper(this)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvSwitchToLogin = findViewById(R.id.tvSwitchToLogin)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            registerUser()
        }

        tvSwitchToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (username.length < 3) {
            Toast.makeText(this, getString(R.string.username_too_short), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si el nombre de usuario ya existe
        if (database.isUsernameExists(username)) {
            Toast.makeText(this, getString(R.string.username_already_exists), Toast.LENGTH_SHORT).show()
            return
        }

        // Deshabilitar el botón para evitar múltiples registros
        btnRegister.isEnabled = false
        Toast.makeText(this, getString(R.string.creating_account), Toast.LENGTH_SHORT).show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Actualizar el perfil del usuario con el nombre de usuario
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            // Enviar email de verificación
                            user.sendEmailVerification()
                                .addOnCompleteListener { emailTask ->
                                    if (emailTask.isSuccessful) {
                                        // Guardar el usuario en la base de datos local
                                        database.insertUser(username, email)

                                        Toast.makeText(
                                            this,
                                            getString(R.string.verification_email_sent),
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // Ir a la actividad de verificación
                                        val intent = Intent(this, EmailVerificationActivity::class.java)
                                        intent.putExtra("email", email)
                                        intent.putExtra("username", username)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            getString(R.string.failed_send_verification) + ": ${emailTask.exception?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        btnRegister.isEnabled = true
                                    }
                                }
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.registration_failed) + ": ${profileTask.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            btnRegister.isEnabled = true
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.registration_failed) + ": ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    btnRegister.isEnabled = true
                }
            }
    }
}