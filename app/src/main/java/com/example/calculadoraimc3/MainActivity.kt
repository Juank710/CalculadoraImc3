package com.example.calculadoraimc3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseHelper
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var btnCalculate: Button
    private lateinit var btnHistory: Button
    private lateinit var btnLogout: Button
    private lateinit var tvResult: TextView
    private lateinit var tvUserName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = DatabaseHelper(this)

        initViews()
        setupClickListeners()

        // Mostrar nombre del usuario
        displayUsername()
    }

    private fun displayUsername() {
        val currentUser = auth.currentUser
        val username = currentUser?.displayName
            ?: database.getUsernameByEmail(currentUser?.email ?: "")
            ?: currentUser?.email?.split("@")?.get(0)
            ?: "Usuario"
        tvUserName.text = "${getString(R.string.welcome)} $username"
    }

    private fun initViews() {
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnHistory = findViewById(R.id.btnHistory)
        btnLogout = findViewById(R.id.btnLogout)
        tvResult = findViewById(R.id.tvResult)
        tvUserName = findViewById(R.id.tvUserName)
    }

    private fun setupClickListeners() {
        btnCalculate.setOnClickListener {
            calculateBMI()
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun calculateBMI() {
        val heightStr = etHeight.text.toString()
        val weightStr = etWeight.text.toString()

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val height = heightStr.toDouble() / 100 // convertir cm a metros
            val weight = weightStr.toDouble()

            val bmi = weight / (height.pow(2))
            val category = getBMICategory(bmi)

            val result = "${getString(R.string.your_bmi)}: %.2f\n${getString(R.string.category)}: $category".format(bmi)
            tvResult.text = result

            // Guardar en base de datos
            saveBMIRecord(height * 100, weight, bmi)

        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.invalid_values), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> getString(R.string.underweight)
            bmi < 25 -> getString(R.string.normal_weight)
            bmi < 30 -> getString(R.string.overweight)
            else -> getString(R.string.obese)
        }
    }

    private fun saveBMIRecord(height: Double, weight: Double, bmi: Double) {
        val currentUser = auth.currentUser
        val username = currentUser?.displayName
            ?: database.getUsernameByEmail(currentUser?.email ?: "")
            ?: currentUser?.email?.split("@")?.get(0)
            ?: "Usuario"
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val record = BMIRecord(
            userName = username,
            height = height,
            weight = weight,
            bmi = bmi,
            dateTime = dateTime
        )

        database.insertBMIRecord(record)
    }
}