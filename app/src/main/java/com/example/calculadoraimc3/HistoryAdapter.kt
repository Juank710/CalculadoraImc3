package com.example.calculadoraimc3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val records: List<BMIRecord>,
    private val showUserName: Boolean = true
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvHeight: TextView = view.findViewById(R.id.tvHeight)
        val tvWeight: TextView = view.findViewById(R.id.tvWeight)
        val tvBMI: TextView = view.findViewById(R.id.tvBMI)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        val context = holder.itemView.context

        holder.tvDateTime.text = record.dateTime

        // Solo mostrar el nombre de usuario si showUserName es true
        if (showUserName) {
            holder.tvUserName.text = "${context.getString(R.string.user)}: ${record.userName}"
            holder.tvUserName.visibility = View.VISIBLE
        } else {
            holder.tvUserName.visibility = View.GONE
        }

        holder.tvHeight.text = "${context.getString(R.string.height)}: %.1f cm".format(record.height)
        holder.tvWeight.text = "${context.getString(R.string.weight)}: %.1f kg".format(record.weight)
        holder.tvBMI.text = "${context.getString(R.string.bmi)}: %.2f".format(record.bmi)
    }

    override fun getItemCount() = records.size
}