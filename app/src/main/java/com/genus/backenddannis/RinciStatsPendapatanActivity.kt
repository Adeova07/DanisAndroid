package com.genus.backenddannis

import android.content.Intent
import android.graphics.Color
import kotlin.random.Random
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.data.AppDatabase
import com.genus.backenddannis.data.adapter.CombineAdapter
import com.genus.backenddannis.data.entity.Pendapatan
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class RinciStatsPendapatanActivity : AppCompatActivity() {

    private lateinit var recyclerViewStatsPendapatan: RecyclerView
    private lateinit var adapter: CombineAdapter
    private lateinit var transaksiList: MutableList<Any>
    private lateinit var pendapatans: List<Pendapatan>
    private lateinit var dateDisplayTextView: TextView
    private lateinit var previousDateButton: ImageButton
    private lateinit var nextDateButton: ImageButton
    private lateinit var pendapatanChart: PieChart
    private var currentMonth: Int = 0
    private var currentYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rinci_stats_pendapatan)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        recyclerViewStatsPendapatan = findViewById(R.id.recvstatsPendapatan)
        dateDisplayTextView = findViewById(R.id.date_display)
        previousDateButton = findViewById(R.id.previousDateButton)
        nextDateButton = findViewById(R.id.nextDateButton)
        pendapatanChart = findViewById(R.id.PendapatanChart)

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

        // Di dalam metode onCreate() dari RinciStasPengeluaranActivity

        val btnPengeluaran: Button = findViewById(R.id.btn_pengeluaran)
        btnPengeluaran.setOnClickListener {
            val intent = Intent(this, RinciStasPengeluaranActivity::class.java)
            startActivity(intent)
        }

        val kembali: ImageButton = findViewById(R.id.backtostatistik)
        kembali.setOnClickListener {
            val intent = Intent(this, StatistikActivity::class.java)
            startActivity(intent)
        }


        GlobalScope.launch(Dispatchers.Main) {
            val pendapatanDao = AppDatabase.getDatabase(this@RinciStatsPendapatanActivity).pendapatanDao()
            pendapatans = pendapatanDao.getAll()

            transaksiList = mutableListOf()
            transaksiList.addAll(pendapatans)

            val sortedTransaksiList = transaksiList.sortedBy { transaction ->
                when (transaction) {
                    is Pendapatan -> transaction.tanggalP
                    else -> throw IllegalArgumentException("Invalid transaction type")
                }
            }



            adapter = CombineAdapter(sortedTransaksiList as MutableList<Any>)
            recyclerViewStatsPendapatan.adapter = adapter
            recyclerViewStatsPendapatan.layoutManager = LinearLayoutManager(this@RinciStatsPendapatanActivity)

            // Menyiapkan data untuk grafik lingkaran
            setupPieChart()
        }
    }

    private fun setupPieChart() {
        GlobalScope.launch(Dispatchers.Main) {
            val pendapatanDao = AppDatabase.getDatabase(this@RinciStatsPendapatanActivity).pendapatanDao()
            val pendapatans = pendapatanDao.getAll()

            // Menghitung total pendapatan
            val totalPendapatan = pendapatans.sumByDouble { it.jumlahP.toDouble() }

            // Membuat map untuk mengelompokkan jumlah pendapatan berdasarkan kategori
            val pendapatanPerKategori = mutableMapOf<String, Float>()
            pendapatans.forEach { pendapatan ->
                val total = pendapatanPerKategori[pendapatan.kategoriP]
                if (total == null) {
                    pendapatanPerKategori[pendapatan.kategoriP] = pendapatan.jumlahP.toFloat()
                } else {
                    pendapatanPerKategori[pendapatan.kategoriP] = total + pendapatan.jumlahP.toFloat()
                }
            }

            val entries = mutableListOf<PieEntry>()
            pendapatanPerKategori.forEach { (kategori, jumlah) ->
                // Menghitung persentase jumlah pendapatan terhadap total pendapatan
                val persentase = (jumlah / totalPendapatan * 100).toFloat()
                entries.add(PieEntry(persentase, kategori))
            }

            val dataSet = PieDataSet(entries, "")
            val colors = mutableListOf<Int>()

            // Generate warna secara acak dan tambahkan ke dalam list warna
            for (i in 0 until pendapatanPerKategori.size) {
                val color = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
                colors.add(color)
            }

            dataSet.colors = colors
            dataSet.valueTextSize = 14f // Atur ukuran teks di dalam pie chart

            val data = PieData(dataSet)

            // Atur formatter untuk menambahkan tanda persen (%) pada teks nilai
            data.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}%"
                }
            })

            pendapatanChart.data = data
            pendapatanChart.description.isEnabled = false
            pendapatanChart.invalidate()
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
        adapter.filterByMonthAndYear(currentMonth, currentYear)

        if (filteredTransactions.isEmpty()) {
            recyclerViewStatsPendapatan.visibility = View.GONE
            // Tampilkan pie chart dengan data 0% jika tidak ada transaksi
            val emptyEntry = PieEntry(100f, "NULL")
            val emptyDataSet = PieDataSet(listOf(emptyEntry), "")
            emptyDataSet.colors = listOf(Color.GRAY)
            val emptyData = PieData(emptyDataSet)
            pendapatanChart.data = emptyData
            pendapatanChart.description.isEnabled = false
            pendapatanChart.invalidate()
        } else {
            recyclerViewStatsPendapatan.visibility = View.VISIBLE
            // Update grafik lingkaran dengan data yang sesuai
            setupPieChart()
        }
    }

    private fun filterTransactionsByMonthAndYear(month: Int, year: Int): List<Any> {
        return transaksiList.filter { transaction ->
            if (transaction is Pendapatan) {
                val cal = Calendar.getInstance()
                cal.time = transaction.tanggalP
                cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year
            } else {
                false
            }
        }
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
}
