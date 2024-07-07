package com.genus.backenddannis


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.data.AppDatabase
import com.genus.backenddannis.data.adapter.CombineAdapter
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class RingkasanActivity : AppCompatActivity() {

    private lateinit var jmlPendapatanTextView: TextView
    private lateinit var jmlPengeluaranTextView: TextView
    private lateinit var jmlTotalTextView: TextView
    private lateinit var dateDisplayTextView: TextView
    private lateinit var previousDateButton: ImageButton
    private lateinit var nextDateButton: ImageButton
    private lateinit var recyclerViewRingkasan: RecyclerView
    private lateinit var adapter: CombineAdapter
    private var transaksiList = mutableListOf<Any>()
    private lateinit var pendapatans: List<Pendapatan>
    private lateinit var pengeluarans: List<Pengeluaran>

    private var currentMonth: Int = 0
    private var currentYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ringkasan)


        // Menambahkan window flag FLAG_LAYOUT_NO_LIMITS
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)


        recyclerViewRingkasan = findViewById(R.id.recvringkas)
        jmlPendapatanTextView = findViewById(R.id.jml_pendapatan)
        jmlPengeluaranTextView = findViewById(R.id.jml_pengeluaran)
        jmlTotalTextView = findViewById(R.id.jml_total)
        dateDisplayTextView = findViewById(R.id.date_display)
        previousDateButton = findViewById(R.id.previousDateButton)
        nextDateButton = findViewById(R.id.nextDateButton)

        val calendar = Calendar.getInstance()
        currentMonth = calendar.get(Calendar.MONTH) + 1
        currentYear = calendar.get(Calendar.YEAR)
        dateDisplayTextView.text = "${getMonthName(currentMonth)} $currentYear"

        previousDateButton.setOnClickListener {
            changeMonth(-1)
        }

        nextDateButton.setOnClickListener {
            changeMonth(1)
        }

        val backButton: ImageButton = findViewById(R.id.backtostatistik)

        backButton.setOnClickListener {
            // Intent untuk pindah ke StatistikActivity
            val intent = Intent(this, StatistikActivity::class.java)
            startActivity(intent)
        }

        GlobalScope.launch(Dispatchers.Main) {
            val pendapatanDao = AppDatabase.getDatabase(this@RingkasanActivity).pendapatanDao()
            val pengeluaranDao = AppDatabase.getDatabase(this@RingkasanActivity).pengeluaranDao()

            val pendapatans = pendapatanDao.getAll()
            val pengeluarans = pengeluaranDao.getAll()

            transaksiList.addAll(pendapatans)
            transaksiList.addAll(pengeluarans)

            val sortedTransaksiList = transaksiList.sortedBy { transaction ->
                when (transaction) {
                    is Pendapatan -> transaction.tanggalP
                    is Pengeluaran -> transaction.tanggal
                    else -> throw IllegalArgumentException("Invalid transaction type")
                }
            }

            adapter = CombineAdapter(sortedTransaksiList as MutableList<Any>)
            recyclerViewRingkasan.adapter = adapter
            recyclerViewRingkasan.layoutManager = LinearLayoutManager(this@RingkasanActivity)

            updateTransactionSummary(pendapatans, pengeluarans)
        }
    }
        private fun updateTransactionSummary(
        pendapatans: List<Pendapatan>,
        pengeluarans: List<Pengeluaran>
    ) {
        val totalPendapatan = pendapatans.sumByDouble { it.jumlahP.toDouble() }
        val totalPengeluaran = pengeluarans.sumByDouble { it.jumlah.toDouble() }
        val total = totalPendapatan - totalPengeluaran

        val formattedPendapatan =
            NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(totalPendapatan)
        val formattedPengeluaran =
            NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(totalPengeluaran)
        val formattedTotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(total)

        jmlPendapatanTextView.text = formattedPendapatan
        jmlPengeluaranTextView.text = formattedPengeluaran
        jmlTotalTextView.text = formattedTotal
    }


    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maret"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Agustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Desember"
            else -> "Bulan tidak valid"
        }
    }

    private fun changeMonth(change: Int) {
        currentMonth += change
        if (currentMonth < 1) {
            currentMonth = 12
            currentYear--
        } else if (currentMonth > 12) {
            currentMonth = 1
            currentYear++
        }
        dateDisplayTextView.text = "${getMonthName(currentMonth)} $currentYear"

        val filteredTransactions = filterTransactionsByMonthAndYear(currentMonth, currentYear)
        if (filteredTransactions.isEmpty()) {
            recyclerViewRingkasan.visibility = View.GONE
            // Kosongkan jumlah pendapatan, pengeluaran, dan total
            jmlPendapatanTextView.text = "Rp 0"
            jmlPengeluaranTextView.text = "Rp 0"
            jmlTotalTextView.text = "Rp 0"
        } else {
            recyclerViewRingkasan.visibility = View.VISIBLE
            adapter.filterByMonthAndYear(currentMonth, currentYear)
            // Perbarui jumlah pendapatan, pengeluaran, dan total
            updateTransactionSummary(
                filteredTransactions.filterIsInstance<Pendapatan>(),
                filteredTransactions.filterIsInstance<Pengeluaran>()
            )
        }
    }



    private fun filterTransactionsByMonthAndYear(month: Int, year: Int): List<Any> {
        val filteredTransactions = mutableListOf<Any>()
        for (transaction in transaksiList) {
            if (transaction is Pendapatan) {
                if (transaction.tanggalP?.month?.let { it + 1 } == month && transaction.tanggalP?.year?.let { it + 1900 } == year)
                {
                    filteredTransactions.add(transaction)
                }
            } else if (transaction is Pengeluaran) {
                if (transaction.tanggal?.month?.let { it + 1 } == month && transaction.tanggal?.year?.let { it + 1900 } == year)
                {
                    filteredTransactions.add(transaction)
                }
            }
        }
        return filteredTransactions
    }
}

