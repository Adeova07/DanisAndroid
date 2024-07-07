package com.genus.backenddannis

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.genus.backenddannis.data.AppDatabase
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import java.text.SimpleDateFormat
import java.util.*

class LinePengeluaranActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_pengeluaran)

        lineChart = findViewById(R.id.lineChart)
        val dateDisplay = findViewById<TextView>(R.id.date_display)
        val previousDateButton = findViewById<ImageButton>(R.id.previousDateButton)
        val nextDateButton = findViewById<ImageButton>(R.id.nextDateButton)

        // Mendapatkan tanggal hari ini
        val todayCalendar = Calendar.getInstance()

        // Jika hari ini bukan Senin, mundurkan tanggal ke Senin sebelumnya
        while (todayCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            todayCalendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        // Tampilkan rentang tanggal awal saat ini
        val initialStartDate = todayCalendar.time
        displayWeekRange(initialStartDate, dateDisplay)

        // Mendapatkan instance dari database
        val appDatabase = AppDatabase.getDatabase(this)

        // Setelah mendapatkan data pengeluaran dan pendapatan, sebelum memperbarui grafik
        val recommendationAI = RecommendationAI()
        val recomendedTextView = findViewById<TextView>(R.id.recomended)

        // Mendapatkan data pengeluaran dan pendapatan berdasarkan rentang tanggal yang baru
        var pengeluaranList = appDatabase.pengeluaranDao().getAllByDateRange(initialStartDate, calculateEndDate(initialStartDate))
        var pendapatanList = appDatabase.pendapatanDao().getAllByDateRange(initialStartDate, calculateEndDate(initialStartDate))

        // Mengatur data untuk LineChart
        setupLineChart(pengeluaranList, pendapatanList)

        // Listener untuk tombol previousDateButton
        previousDateButton.setOnClickListener {
            // Mundurkan tanggal ke minggu sebelumnya
            todayCalendar.add(Calendar.DAY_OF_MONTH, -7)

            // Tampilkan rentang tanggal yang baru
            val newStartDate = todayCalendar.time
            displayWeekRange(newStartDate, dateDisplay)

            // Mendapatkan data pengeluaran dan pendapatan berdasarkan rentang tanggal yang baru
            pengeluaranList = appDatabase.pengeluaranDao().getAllByDateRange(newStartDate, calculateEndDate(newStartDate))
            pendapatanList = appDatabase.pendapatanDao().getAllByDateRange(newStartDate, calculateEndDate(newStartDate))

            // Membuat saran berdasarkan data pengeluaran dan pendapatan
            val saran = recommendationAI.getRecommendation(pengeluaranList, pendapatanList)

            // Menampilkan saran pada TextView
            recomendedTextView.text = saran

            // Memperbarui data grafik dengan rentang tanggal yang baru
            updateChartData(pengeluaranList, pendapatanList)
        }

             // Listener untuk tombol nextDateButton
            nextDateButton.setOnClickListener {
            // Majukan tanggal ke minggu berikutnya
            todayCalendar.add(Calendar.DAY_OF_MONTH, 7)

            // Tampilkan rentang tanggal yang baru
            val newStartDate = todayCalendar.time
            displayWeekRange(newStartDate, dateDisplay)

            // Mendapatkan data pengeluaran dan pendapatan berdasarkan rentang tanggal yang baru
            pengeluaranList = appDatabase.pengeluaranDao().getAllByDateRange(newStartDate, calculateEndDate(newStartDate))
            pendapatanList = appDatabase.pendapatanDao().getAllByDateRange(newStartDate, calculateEndDate(newStartDate))

            // Membuat saran berdasarkan data pengeluaran dan pendapatan
                val saran = recommendationAI.getRecommendation(pengeluaranList, pendapatanList)

            // Menampilkan saran pada TextView
            recomendedTextView.text = saran

            // Memperbarui data grafik dengan rentang tanggal yang baru
            updateChartData(pengeluaranList, pendapatanList)
        }
    }

    // Fungsi untuk menampilkan rentang tanggal pada TextView
    private fun displayWeekRange(startDate: Date, dateDisplay: TextView) {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val startDateStr = dateFormat.format(startDate)

        // Tambahkan 6 hari ke depan dari tanggal awal untuk mendapatkan tanggal Minggu
        val endDateCalendar = Calendar.getInstance()
        endDateCalendar.time = startDate
        endDateCalendar.add(Calendar.DAY_OF_MONTH, 6)
        val endDate = endDateCalendar.time
        val endDateStr = dateFormat.format(endDate)

        // Set teks pada TextView dengan rentang tanggal yang baru
        dateDisplay.text = "$startDateStr - $endDateStr"
    }

    // Fungsi untuk memperbarui data grafik dengan rentang tanggal yang baru
    private fun updateChartData(pengeluaranList: List<Pengeluaran>, pendapatanList: List<Pendapatan>) {
        // Mengatur data untuk LineChart
        setupLineChart(pengeluaranList, pendapatanList)
    }

    // Fungsi untuk menghitung tanggal akhir rentang berdasarkan tanggal awal
    private fun calculateEndDate(startDate: Date): Date {
        val endDateCalendar = Calendar.getInstance()
        endDateCalendar.time = startDate
        endDateCalendar.add(Calendar.DAY_OF_MONTH, 6)
        return endDateCalendar.time
    }

    private fun setupLineChart(pengeluaranList: List<Pengeluaran>, pendapatanList: List<Pendapatan>) {
        val pengeluaranEntries = MutableList(7) { Entry(it.toFloat(), 0f) } // Initialize entries for 7 days (Mon-Sun)
        val pendapatanEntries = MutableList(7) { Entry(it.toFloat(), 0f) }

        val calendar = Calendar.getInstance()

        // Mengisi data untuk pengeluaran
        for (pengeluaran in pengeluaranList) {
            calendar.time = pengeluaran.tanggal
            val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Adjust to start from Monday
            pengeluaranEntries[dayOfWeek] = Entry(dayOfWeek.toFloat(), pengeluaranEntries[dayOfWeek].y + pengeluaran.jumlah.toFloat())
        }

        // Mengisi data untuk pendapatan
        for (pendapatan in pendapatanList) {
            calendar.time = pendapatan.tanggalP
            val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Adjust to start from Monday
            pendapatanEntries[dayOfWeek] = Entry(dayOfWeek.toFloat(), pendapatanEntries[dayOfWeek].y + pendapatan.jumlahP.toFloat())
        }

        // Membuat LineDataSet untuk pengeluaran
        val pengeluaranDataSet = LineDataSet(pengeluaranEntries, "Pengeluaran")
        pengeluaranDataSet.color = resources.getColor(R.color.redFlag)
        pengeluaranDataSet.valueTextColor = resources.getColor(R.color.redFlag)
        pengeluaranDataSet.lineWidth = 2f

        // Membuat LineDataSet untuk pendapatan
        val pendapatanDataSet = LineDataSet(pendapatanEntries, "Pendapatan")
        pendapatanDataSet.color = resources.getColor(R.color.menu_icon_color)
        pendapatanDataSet.valueTextColor = resources.getColor(R.color.menu_icon_color)
        pendapatanDataSet.lineWidth = 2f

        // Menambahkan data ke LineChart
        val lineData = LineData(pengeluaranDataSet, pendapatanDataSet)
        lineChart.data = lineData

        // Mengatur tampilan XAxis untuk menampilkan nama hari
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = XAxisValueFormatter()
        xAxis.setDrawGridLines(false)

        // Mengatur YAxis
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawLabels(true)

        val rightAxis = lineChart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawLabels(false)

        // Mengatur legend dan deskripsi
        lineChart.legend.isEnabled = true
        lineChart.description.isEnabled = false

        // Memperbarui chart
        lineChart.invalidate()
    }

    // Formatter untuk XAxis agar menampilkan nama hari
    inner class XAxisValueFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
        private val days = arrayOf("Senin", "Selasa", "Rabu", "Kamis", "Jum'at", "Sabtu", "Minggu")

        override fun getFormattedValue(value: Float): String {
            return days[(value.toInt()) % days.size]
        }
    }
}
