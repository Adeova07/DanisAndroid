package com.genus.backenddannis.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.R
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import java.text.SimpleDateFormat
import java.util.*

class TLAdapter(
    private val context: Context,
    private var pengeluaranList: MutableList<Pengeluaran>,
    private var pendapatanList: MutableList<Pendapatan>
) : RecyclerView.Adapter<TLAdapter.TLViewHolder>() {

    inner class TLViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayDisplay: TextView = itemView.findViewById(R.id.day_display)
        val dateDisplay: TextView = itemView.findViewById(R.id.date_display)
        val balance: TextView = itemView.findViewById(R.id.Balance)
        val jmlTransaksi: TextView = itemView.findViewById(R.id.jml_transaksi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TLViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recvpengeluaran, parent, false)
        return TLViewHolder(view)
    }

    override fun onBindViewHolder(holder: TLViewHolder, position: Int) {
        val pengeluaran = pengeluaranList[position]
        val pendapatan = pendapatanList[position]

        val date = pengeluaran.tanggal // Assume both lists are synchronized and have the same dates
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Menampilkan hari
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayString = dayFormat.format(date)
        holder.dayDisplay.text = dayString

        // Menampilkan tanggal, bulan, dan tahun
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(date)
        holder.dateDisplay.text = dateString

        // Menghitung Balance
        val totalPengeluaran = pengeluaranList.sumByDouble { it.jumlah.toDouble() }
        val totalPendapatan = pendapatanList.sumByDouble { it.jumlahP.toDouble() }
        val balance = totalPendapatan - totalPengeluaran
        holder.balance.text = balance.toString()

        // Menghitung jumlah transaksi pada tanggal tertentu
        val transaksiPengeluaran = pengeluaranList.count { it.tanggal == date }
        val transaksiPendapatan = pendapatanList.count { it.tanggalP == date }
        val totalTransaksi = transaksiPengeluaran + transaksiPendapatan
        holder.jmlTransaksi.text = totalTransaksi.toString()
    }

    fun updateData(newPengeluaranList: List<Pengeluaran>, newPendapatanList: List<Pendapatan>) {
        pengeluaranList = newPengeluaranList.toMutableList()
        pendapatanList = newPendapatanList.toMutableList()
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return pengeluaranList.size
    }
}

