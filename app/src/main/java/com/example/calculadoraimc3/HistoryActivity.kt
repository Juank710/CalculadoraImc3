package com.example.calculadoraimc3

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class HistoryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var tvHistoryTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        auth = FirebaseAuth.getInstance()
        database = DatabaseHelper(this)

        initViews()
        loadHistory()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewHistory)
        tvHistoryTitle = findViewById(R.id.tvHistoryTitle)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadHistory() {
        val currentUser = auth.currentUser
        val username = currentUser?.displayName
            ?: database.getUsernameByEmail(currentUser?.email ?: "")
            ?: currentUser?.email?.split("@")?.get(0)
            ?: "Usuario"

        // Actualizar el título con el nombre de usuario
        tvHistoryTitle.text = "${getString(R.string.bmi_history_of)} $username"

        val records = database.getBMIRecordsByUser(username)

        if (records.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_records), Toast.LENGTH_SHORT).show()
        }

        adapter = HistoryAdapter(records, showUserName = false) // No mostrar username en cada item
        recyclerView.adapter = adapter
    }
}