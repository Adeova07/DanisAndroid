package com.genus.backenddannis.data.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.R
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PPadapter(private var itemtransaksi: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0

    fun filterByMonthAndYear(month: Int, year: Int) {
        selectedMonth = month
        selectedYear = year
        notifyDataSetChanged() // Memperbarui tampilan RecyclerView setelah filter diterapkan
    }

    fun setData(newData: List<Any>) {
        itemtransaksi = newData
        notifyDataSetChanged()
    }

    companion object {
        private const val TYPE_PENDAPATAN = 1
        private const val TYPE_PENGELUARAN = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PENDAPATAN -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.recvpengeluaran, parent, false)
                PendapatanViewHolder(view)
            }

            TYPE_PENGELUARAN -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.recvpengeluaran, parent, false)
                PengeluaranViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PendapatanViewHolder -> {
                val pendapatan = itemtransaksi[position] as Pendapatan
                holder.bind(pendapatan)
            }

            is PengeluaranViewHolder -> {
                val pengeluaran = itemtransaksi[position] as Pengeluaran
                holder.bind(pengeluaran)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemtransaksi.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemtransaksi[position] is Pendapatan) {
            TYPE_PENDAPATAN
        } else {
            TYPE_PENGELUARAN
        }
    }

    inner class PendapatanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val kategoriTextView: TextView = itemView.findViewById(R.id.kategoriID)
        private val deskripsiTextView: TextView = itemView.findViewById(R.id.deskripsiID)
        private val balanceTextView: TextView = itemView.findViewById(R.id.Balance)
        private val jamTextView: TextView = itemView.findViewById(R.id.jamid)

        fun bind(pendapatan: Pendapatan) {
            kategoriTextView.text = pendapatan.kategoriP
            deskripsiTextView.text = pendapatan.deskripsiP
            balanceTextView.text = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                .format(pendapatan.jumlahP.toDouble())
            balanceTextView.setTextColor(Color.BLUE) // Set text color to blue for pendapatan

            // Check if the date is not null before formatting it
            pendapatan.tanggalP?.let { tanggal ->
                val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val formattedDate = dateFormat.format(tanggal)
                jamTextView.text = formattedDate // Menetapkan jam ke TextView
            }
        }
    }

    inner class PengeluaranViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val kategoriTextView: TextView = itemView.findViewById(R.id.kategoriID)
        private val deskripsiTextView: TextView = itemView.findViewById(R.id.deskripsiID)
        private val balanceTextView: TextView = itemView.findViewById(R.id.Balance)
        private val jamTextView: TextView = itemView.findViewById(R.id.jamid)

        fun bind(pengeluaran: Pengeluaran) {
            kategoriTextView.text = pengeluaran.kategori
            deskripsiTextView.text = pengeluaran.deskripsi
            balanceTextView.text = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                .format(pengeluaran.jumlah.toDouble())
            balanceTextView.setTextColor(Color.RED) // Set text color to red for pengeluaran

            // Check if the date is not null before formatting it
            pengeluaran.tanggal?.let { tanggal ->
                val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val formattedDate = dateFormat.format(tanggal)
                jamTextView.text = formattedDate // Menetapkan jam ke TextView
            }
        }
    }
}


