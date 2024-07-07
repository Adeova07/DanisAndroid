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
import com.genus.backenddannis.data.entity.Pengeluaran
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class RinciStasPengeluaranActivity : AppCompatActivity() {

    private lateinit var recyclerViewStatisPengeluaran: RecyclerView
    private lateinit var adapter: CombineAdapter
    private lateinit var transaksiList: MutableList<Any>
    private lateinit var pengeluarans: List<Pengeluaran>
    private lateinit var dateDisplayTextView: TextView
    private lateinit var previousDateButton: ImageButton
    private lateinit var nextDateButton: ImageButton
    private lateinit var pengeluaranChart: PieChart
    private var currentMonth: Int = 0
    private var currentYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rinci_stas_pengeluaran)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        recyclerViewStatisPengeluaran = findViewById(R.id.recvstatisPengeluaran)
        dateDisplayTextView = findViewById(R.id.date_display)
        previousDateButton = findViewById(R.id.previousDateButton)
        nextDateButton = findViewById(R.id.nextDateButton)
        pengeluaranChart = findViewById(R.id.PengeluaranChart)

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

        val btnPendapatan: Button = findViewById(R.id.btn_pendapatan)
        btnPendapatan.setOnClickListener {
            val intent = Intent(this, RinciStatsPendapatanActivity::class.java)
            startActivity(intent)
        }

        val kembali: ImageButton = findViewById(R.id.backtostatistik)
        kembali.setOnClickListener {
            val intent = Intent(this, StatistikActivity::class.java)
            startActivity(intent)
        }


        GlobalScope.launch(Dispatchers.Main) {
            val pengeluaranDao = AppDatabase.getDatabase(this@RinciStasPengeluaranActivity).pengeluaranDao()
            pengeluarans = pengeluaranDao.getAll()

            transaksiList = mutableListOf()
            transaksiList.addAll(pengeluarans)

            val sortedTransaksiList = transaksiList.sortedBy { transaction ->
                when (transaction) {
                    is Pengeluaran -> transaction.tanggal
                    else -> throw IllegalArgumentException("Invalid transaction type")
                }
            }

            adapter = CombineAdapter(sortedTransaksiList as MutableList<Any>)
            recyclerViewStatisPengeluaran.adapter = adapter
            recyclerViewStatisPengeluaran.layoutManager = LinearLayoutManager(this@RinciStasPengeluaranActivity)

            // Menyiapkan data untuk grafik lingkaran
            setupPieChart()
        }
    }

    private fun setupPieChart() {
        GlobalScope.launch(Dispatchers.Main) {
            val pengeluaranDao = AppDatabase.getDatabase(this@RinciStasPengeluaranActivity).pengeluaranDao()
            val pengeluarans = pengeluaranDao.getAll()

            // Menghitung total pengeluaran
            val totalPengeluaran = pengeluarans.sumByDouble { it.jumlah.toDouble() }

            // Membuat map untuk mengelompokkan jumlah pengeluaran berdasarkan kategori
            val pengeluaranPerKategori = mutableMapOf<String, Float>()
            pengeluarans.forEach { pengeluaran ->
                val total = pengeluaranPerKategori[pengeluaran.kategori]
                if (total == null) {
                    pengeluaranPerKategori[pengeluaran.kategori] = pengeluaran.jumlah.toFloat()
                } else {
                    pengeluaranPerKategori[pengeluaran.kategori] = total + pengeluaran.jumlah.toFloat()
                }
            }

            val entries = mutableListOf<PieEntry>()
            pengeluaranPerKategori.forEach { (kategori, jumlah) ->
                // Menghitung persentase jumlah pengeluaran terhadap total pengeluaran
                val persentase = (jumlah / totalPengeluaran * 100).toFloat()
                entries.add(PieEntry(persentase, kategori))
            }

            val dataSet = PieDataSet(entries, "")
            val colors = mutableListOf<Int>()

            // Generate warna secara acak dan tambahkan ke dalam list warna
            for (i in 0 until pengeluaranPerKategori.size) {
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

            pengeluaranChart.data = data
            pengeluaranChart.description.isEnabled = false
            pengeluaranChart.invalidate()
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
            recyclerViewStatisPengeluaran.visibility = View.GONE
            // Tampilkan pie chart dengan data 0% jika tidak ada transaksi
            val emptyEntry = PieEntry(100f, "NULL")
            val emptyDataSet = PieDataSet(listOf(emptyEntry), "")
            emptyDataSet.colors = listOf(Color.GRAY)
            val emptyData = PieData(emptyDataSet)
            pengeluaranChart.data = emptyData
            pengeluaranChart.description.isEnabled = false
            pengeluaranChart.invalidate()
        } else {
            recyclerViewStatisPengeluaran.visibility = View.VISIBLE
            // Update grafik lingkaran dengan data yang sesuai
            setupPieChart()
        }
    }


    private fun filterTransactionsByMonthAndYear(month: Int, year: Int): List<Any> {
        return transaksiList.filter { transaction ->
            if (transaction is Pengeluaran) {
                val cal = Calendar.getInstance()
                cal.time = transaction.tanggal
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
