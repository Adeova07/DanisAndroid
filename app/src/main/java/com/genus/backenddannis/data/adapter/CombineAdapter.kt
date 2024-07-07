package com.genus.backenddannis.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.R
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import com.genus.backenddannis.data.entity.Transaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CombineAdapter(var transactionList: MutableList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var lastDateDisplayed: Date? = null
    private val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recvutama, parent, false)
        return when (viewType) {
            TYPE_PENDAPATAN -> PendapatanViewHolder(view)
            TYPE_PENGELUARAN -> PengeluaranViewHolder(view)
            TYPE_TRANSAKSI -> TransaksiViewHolder(view)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PendapatanViewHolder -> holder.bind(transactionList[position] as Pendapatan)
            is PengeluaranViewHolder -> holder.bind(transactionList[position] as Pengeluaran)
            is TransaksiViewHolder -> holder.bind(transactionList[position] as Transaksi)
        }
    }

    override fun getItemCount(): Int = transactionList.size

    fun removeItemAt(position: Int) {
        transactionList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun filterByMonthAndYear(month: Int, year: Int) {
        // Lakukan filter sesuai dengan bulan dan tahun yang dipilih
        transactionList = transactionList.filter { transaction ->
            if (transaction is Pengeluaran) {
                val cal = Calendar.getInstance()
                cal.time = transaction.tanggal
                cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year
            } else {
                true // Biarkan item yang bukan Pengeluaran tetap ditampilkan
            }
        }.toMutableList()
        notifyDataSetChanged() // Perbarui tampilan setelah proses filtering
    }

    override fun getItemViewType(position: Int): Int {
        return when (transactionList[position]) {
            is Pendapatan -> TYPE_PENDAPATAN
            is Pengeluaran -> TYPE_PENGELUARAN
            is Transaksi -> TYPE_TRANSAKSI
            else -> throw IllegalArgumentException("Invalid transaction type")
        }
    }

    inner class PendapatanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val kategoriTextView: TextView = itemView.findViewById(R.id.deskripsiID)
        private val deskripsiTextView: TextView = itemView.findViewById(R.id.kategoriID)
        private val balanceTextView: TextView = itemView.findViewById(R.id.Balance)
        private val bulanDanTahun: TextView = itemView.findViewById(R.id.tv_date_today)
        private val timeWaktu: TextView = itemView.findViewById(R.id.jamid)
        private val tanggalTextView: TextView = itemView.findViewById(R.id.menampilkanDay)
        private val cardTanggal: CardView = itemView.findViewById(R.id.cardtanggal)
        private val piggyIcon: ImageView = itemView.findViewById(R.id.piggyicon)

        fun bind(pendapatan: Pendapatan) {
            kategoriTextView.text = pendapatan.kategoriP
            deskripsiTextView.text = pendapatan.deskripsiP
            balanceTextView.text = "+${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(pendapatan.jumlahP.toDouble())}"
            balanceTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.menu_icon_color))

            piggyIcon.setImageResource(R.drawable.piggy_bank)

            pendapatan.tanggalP?.let { tanggal ->
                val formattedDate = dateFormat.format(tanggal)
                if (lastDateDisplayed != null && dateFormat.format(lastDateDisplayed) == formattedDate) {
                    cardTanggal.visibility = View.GONE
                } else {
                    cardTanggal.visibility = View.VISIBLE
                    lastDateDisplayed = tanggal
                }

                bulanDanTahun.text = monthYearFormat.format(tanggal)
                timeWaktu.text = timeFormat.format(tanggal)
                tanggalTextView.text = formattedDate
            }
        }
    }

    inner class PengeluaranViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val kategoriTextView: TextView = itemView.findViewById(R.id.deskripsiID)
        private val deskripsiTextView: TextView = itemView.findViewById(R.id.kategoriID)
        private val balanceTextView: TextView = itemView.findViewById(R.id.Balance)
        private val bulanDanTahun: TextView = itemView.findViewById(R.id.tv_date_today)
        private val timeWaktu: TextView = itemView.findViewById(R.id.jamid)
        private val tanggalTextView: TextView = itemView.findViewById(R.id.menampilkanDay)
        private val cardTanggal: CardView = itemView.findViewById(R.id.cardtanggal)
        private val piggyIcon: ImageView = itemView.findViewById(R.id.piggyicon)

        fun bind(pengeluaran: Pengeluaran) {
            kategoriTextView.text = pengeluaran.kategori
            deskripsiTextView.text = pengeluaran.deskripsi
            balanceTextView.text = "-${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(pengeluaran.jumlah.toDouble())}"
            balanceTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.redFlag))

            piggyIcon.setImageResource(R.drawable.iconpiggycrash)

            pengeluaran.tanggal?.let { tanggal ->
                val formattedDate = dateFormat.format(tanggal)
                if (lastDateDisplayed != null && dateFormat.format(lastDateDisplayed) == formattedDate) {
                    cardTanggal.visibility = View.GONE
                } else {
                    cardTanggal.visibility = View.VISIBLE
                    lastDateDisplayed = tanggal
                }

                bulanDanTahun.text = monthYearFormat.format(tanggal)
                timeWaktu.text = timeFormat.format(tanggal)
                tanggalTextView.text = formattedDate
            }
        }
    }

    inner class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val kategori1TextView: TextView = itemView.findViewById(R.id.kategoriID)
        private val kategori2TextView: TextView = itemView.findViewById(R.id.deskripsiID)
        private val jumlahTextView: TextView = itemView.findViewById(R.id.Balance)

        fun bind(transaksi: Transaksi) {
            kategori1TextView.text = transaksi.kategoriDari
            kategori2TextView.text = transaksi.kategoriKepada
            jumlahTextView.text = transaksi.jumlah
        }
    }

    companion object {
        private const val TYPE_PENDAPATAN = 1
        private const val TYPE_PENGELUARAN = 2
        private const val TYPE_TRANSAKSI = 3
    }
}
