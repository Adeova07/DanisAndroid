package com.genus.backenddannis

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.genus.backenddannis.data.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*



class StatistikActivity : AppCompatActivity() {
    private lateinit var userNameTextView: TextView
    private lateinit var dateDisplayTextView: TextView
    private lateinit var jmlPendapatanTextView: TextView
    private lateinit var jmlPengeluaranTextView: TextView
    private lateinit var jmlTotalTextView: TextView
    private lateinit var sldTotalTextView: TextView
    private lateinit var sldPendapatanTextView: TextView



    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistik)

        // Mendapatkan referensi ke TextView
        userNameTextView = findViewById(R.id.tmpl_UserName)
        dateDisplayTextView = findViewById(R.id.date_display)
        jmlPendapatanTextView = findViewById(R.id.jml_pendapatan)
        jmlPengeluaranTextView = findViewById(R.id.jml_pengeluaran)
        jmlTotalTextView = findViewById(R.id.jml_total)
        sldPendapatanTextView = findViewById(R.id.sld_pendapatan)
        // Inisialisasi properti sldTotalTextView
        sldTotalTextView = findViewById(R.id.sld_total)


        // Menambahkan window flag FLAG_LAYOUT_NO_LIMITS
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Mendapatkan referensi ke tombol-tombol date
        val previousDateButton = findViewById<ImageButton>(R.id.previousDateButton)
        val nextDateButton = findViewById<ImageButton>(R.id.nextDateButton)

        // Mendapatkan tanggal saat ini
        val currentDateCal = Calendar.getInstance()

        // Format tanggal untuk ditampilkan
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDateCal.time)

        // Menampilkan tanggal saat ini pada dateDisplayTextView
        dateDisplayTextView.text = formattedDate

        // Memuat data pengguna dan statistik untuk bulan saat ini
        loadUserDataAndStats()

        // Memperbahatui saldo terbesar bulan ini
        loadSaldoTerbesarForMonth(currentDateCal)

        // Memperbahatui total saldo bulan ini
        updateTotalSaldoForMonth(currentDateCal)

        // Memperbarui statistik untuk bulan saat ini
        updateStatsForSelectedMonth(currentDateCal)

        // Mengatur listener untuk tombol sebelumnya
        previousDateButton.setOnClickListener {
            adjustMonth(-1) // Mengurangi 1 bulan dari bulan yang ditampilkan
        }

        // Mengatur listener untuk tombol berikutnya
        nextDateButton.setOnClickListener {
            adjustMonth(1) // Menambah 1 bulan dari bulan yang ditampilkan
        }

        val lRingkasanImageView = findViewById<ImageButton>(R.id.l_ringkasan)
        lRingkasanImageView.setOnClickListener {
            // Membuat intent untuk memulai RingkasanActivity
            val intent = Intent(this, RingkasanActivity::class.java)
            startActivity(intent)
        }

        val btnLineActivity = findViewById<ImageButton>(R.id.lineChart)
        btnLineActivity.setOnClickListener {
            // Membuat intent untuk memulai RingkasanActivity
            val intent = Intent(this, LinePengeluaranActivity::class.java)
            startActivity(intent)
        }

        val gotoRinciStas = findViewById<ImageButton>(R.id.gotoRinciStas)
        gotoRinciStas.setOnClickListener {
            // Membuat intent untuk memulai activity tujuan
            val intent = Intent(this, RinciStasPengeluaranActivity::class.java)
            startActivity(intent)
        }


        val menu1 = findViewById<LinearLayout>(R.id.menu1)
        menu1.setOnClickListener {
            // Membuat intent untuk memulai StatistikActivity
            val intent = Intent(this, UtamaActivity::class.java)
            startActivity(intent)
        }

        // Menambahkan listener onClick pada menu2
        val menu3 = findViewById<LinearLayout>(R.id.menu3)
        menu3.setOnClickListener {
            // Membuat intent untuk memulai StatistikActivity
            val intent = Intent(this, WalletMActivity::class.java)
            startActivity(intent)
        }

        dateDisplayTextView.setOnClickListener {
            showMonthPickerDialog()
        }
    }

    private fun adjustMonth(delta: Int) {
        val displayedDate = dateDisplayTextView.text.toString()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val displayedCal = Calendar.getInstance().apply {
            time = dateFormat.parse(displayedDate)
            add(Calendar.MONTH, delta)
        }
        dateDisplayTextView.text = dateFormat.format(displayedCal.time)

        lifecycleScope.launch {
            val hasTransactions = hasTransactionsForMonth(displayedCal)
            if (!hasTransactions) {
                val pieChart = findViewById<PieChart>(R.id.pieChart)
                pieChart.clear()
                pieChart.invalidate()

                jmlPendapatanTextView.text = "Rp. 0"
                jmlPengeluaranTextView.text = "Rp. 0"
                jmlTotalTextView.text = "Rp. 0"
                sldPendapatanTextView.text = "Rp. 0"
                sldTotalTextView.text = "Rp. 0"

                val isiDatabaseKategori = findViewById<LinearLayout>(R.id.isidatabasekategori)
                isiDatabaseKategori.removeAllViews()
            } else {
                updateStatsForSelectedMonth(displayedCal)
                // Perbarui total saldo untuk bulan saat ini setelah memperbarui statistik
                updateTotalSaldoForMonth(displayedCal)
                // Perbarui saldo terbesar untuk bulan saat ini
                loadSaldoTerbesarForMonth(displayedCal)
            }
        }
    }

    private fun loadUserDataAndStats() {
        lifecycleScope.launch {
            val userDao = AppDatabase.getDatabase(this@StatistikActivity).userDao()
            val user = userDao.getAll().firstOrNull()
            user?.let {
                it.userName?.let { userName ->
                    userNameTextView.text = userName // Menetapkan nama pengguna ke TextView
                }
            }
        }
    }

    private fun loadSaldoTerbesarForMonth(month: Calendar) {
        val pendapatanDao = AppDatabase.getDatabase(this@StatistikActivity).pendapatanDao()

        // Jalankan operasi database secara asinkron menggunakan lifecycleScope
        lifecycleScope.launch {
            // Lakukan operasi database di dalam coroutine
            val saldoTerbesar = withContext(Dispatchers.IO) {
                pendapatanDao.getAll().filter {
                    val cal = Calendar.getInstance()
                    cal.time = it.tanggalP ?: Date()
                    cal.get(Calendar.MONTH) == month.get(Calendar.MONTH)
                }.maxByOrNull { it.jumlahP.toInt() }
            }

            saldoTerbesar?.let {
                val df = DecimalFormat("#,###")
                val saldoTerbesarFormatted = df.format(it.jumlahP.toInt())
                sldPendapatanTextView.text = "Rp. $saldoTerbesarFormatted"
            } ?: run {
                sldPendapatanTextView.text = "Rp. 0"
            }
        }
    }


    private fun updateTotalSaldoForMonth(month: Calendar) {
        val pendapatanDao = AppDatabase.getDatabase(this@StatistikActivity).pendapatanDao()
        val pengeluaranDao = AppDatabase.getDatabase(this@StatistikActivity).pengeluaranDao()

        // Jalankan operasi database secara asinkron menggunakan lifecycleScope
        lifecycleScope.launch {
            // Lakukan operasi database di dalam coroutine
            val totalPendapatan = withContext(Dispatchers.IO) {
                pendapatanDao.getAll().filter {
                    val cal = Calendar.getInstance()
                    cal.time = it.tanggalP ?: Date()
                    cal.get(Calendar.MONTH) == month.get(Calendar.MONTH)
                }.sumBy { it.jumlahP.toInt() }
            }

            val totalPengeluaran = withContext(Dispatchers.IO) {
                pengeluaranDao.getAll().filter {
                    val cal = Calendar.getInstance()
                    cal.time = it.tanggal ?: Date()
                    cal.get(Calendar.MONTH) == month.get(Calendar.MONTH)
                }.sumBy { it.jumlah.toInt() }
            }

            val totalSaldo = totalPendapatan - totalPengeluaran
            val df = DecimalFormat("#,###")
            val totalSaldoFormatted = df.format(totalSaldo)
            sldTotalTextView.text = "Rp. $totalSaldoFormatted"
        }
    }


    private suspend fun hasTransactionsForMonth(month: Calendar): Boolean {
        val pengeluaranDao = AppDatabase.getDatabase(this@StatistikActivity).pengeluaranDao()

        // Menghitung jumlah transaksi pada bulan yang diberikan
        val transactionsCount = pengeluaranDao.getAll().count { transaction ->
            val cal = Calendar.getInstance()
            cal.time = transaction.tanggal ?: Date()
            cal.get(Calendar.MONTH) == month.get(Calendar.MONTH)
        }

        return transactionsCount > 0
    }


    private fun updateStatsForSelectedMonth(selectedMonth: Calendar) {
        lifecycleScope.launch {
            val pendapatanDao = AppDatabase.getDatabase(this@StatistikActivity).pendapatanDao()
            val pengeluaranDao = AppDatabase.getDatabase(this@StatistikActivity).pengeluaranDao()

            // Memeriksa apakah ada transaksi pada bulan yang dipilih
            val hasTransactions = hasTransactionsForMonth(selectedMonth)

            if (hasTransactions) {
                val pengeluaranList = pengeluaranDao.getAll()

                // Mengelompokkan transaksi berdasarkan kategori dan menjumlahkan jumlahnya
                val groupedTransactions = pengeluaranList.groupBy { it.kategori }
                val groupedEntries = ArrayList<PieEntry>()

                for ((kategori, transactions) in groupedTransactions) {
                    val totalAmount = transactions.sumBy { it.jumlah.toInt() }
                    groupedEntries.add(PieEntry(totalAmount.toFloat(), kategori))
                }

                // Membuat dataset baru untuk chart
                val dataSet = PieDataSet(groupedEntries, "")

                val totalPendapatan = pendapatanDao.getAll().filter {
                    val cal = Calendar.getInstance()
                    cal.time = it.tanggalP ?: Date()
                    cal.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH)
                }.sumBy { it.jumlahP.toInt() }

                val totalPengeluaran = pengeluaranDao.getAll().filter {
                    val cal = Calendar.getInstance()
                    cal.time = it.tanggal ?: Date()
                    cal.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH)
                }.sumBy { it.jumlah.toInt() }

                // Memeriksa total pendapatan dan pengeluaran
                if (totalPendapatan == 0 || totalPengeluaran == 0) {
                    jmlPendapatanTextView.text = "Rp. 0"
                    jmlPengeluaranTextView.text = "Rp. 0"
                } else {
                    // Menampilkan jumlah pendapatan dan pengeluaran
                    val df = DecimalFormat("#,###")
                    jmlPendapatanTextView.text = "Rp. ${df.format(totalPendapatan)}"
                    jmlPendapatanTextView.setTextColor(ContextCompat.getColor(this@StatistikActivity, R.color.menu_icon_color)) // Warna biru untuk pendapatan
                    jmlPengeluaranTextView.text = "Rp. ${df.format(totalPengeluaran)}"
                    jmlPengeluaranTextView.setTextColor(ContextCompat.getColor(this@StatistikActivity, R.color.redFlag)) // Warna merah untuk pengeluaran
                    // Menghitung total dan menampilkannya
                    val total = totalPendapatan - totalPengeluaran
                    jmlTotalTextView.text = "Rp. ${df.format(total)}"
                    if (total >= 0) {
                        jmlTotalTextView.setTextColor(ContextCompat.getColor(this@StatistikActivity, R.color.menu_icon_color)) // Warna biru untuk total pendapatan
                    } else {
                        jmlTotalTextView.setTextColor(ContextCompat.getColor(this@StatistikActivity, R.color.redFlag)) // Warna merah untuk total pengeluaran
                    }
                }


                // Mendapatkan referensi ke PieChart
                val pieChart = findViewById<PieChart>(R.id.pieChart)
                val isiDatabaseKategori = findViewById<LinearLayout>(R.id.isidatabasekategori)

                // Mendeklarasikan list untuk menyimpan warna yang dihasilkan
                val generatedColors: MutableList<Int> = mutableListOf()

                // Membersihkan isiDatabaseKategori sebelum menambahkan elemen baru
                isiDatabaseKategori.removeAllViews()

                // Menyiapkan data untuk chart
                val entries: ArrayList<PieEntry> = ArrayList()
                for (pengeluaran in pengeluaranList) {
                    entries.add(PieEntry(pengeluaran.jumlah.toFloat(), pengeluaran.kategori))

                    // Mengatur warna yang telah dihasilkan untuk setiap kategori
                    val generatedColors: MutableList<Int> = mutableListOf()
                    for (i in 0 until groupedEntries.size) {
                        val random = Random()
                        val r = random.nextInt(256)
                        val g = random.nextInt(256)
                        val b = random.nextInt(256)
                        val color = Color.rgb(r, g, b)
                        generatedColors.add(color)
                    }
                    dataSet.colors = generatedColors

                    // Membuat objek PieData
                    val data = PieData(dataSet)
                    val pieChart = findViewById<PieChart>(R.id.pieChart)
                    pieChart.data = data

                    // Menambahkan deskripsi pada chart
                    pieChart.description.text = ""

                    // Menghitung jumlah total pengeluaran dan menampilkannya
                    val totalPengeluaran = groupedEntries.sumBy { it.value.toInt() }
                    jmlPengeluaranTextView.text = "Rp. ${totalPengeluaran}"

                    // Menambahkan kategori ke layout
                    val isiDatabaseKategori = findViewById<LinearLayout>(R.id.isidatabasekategori)
                    isiDatabaseKategori.removeAllViews()
                    for (entry in groupedEntries) {
                        val colorView = View(this@StatistikActivity)
                        colorView.layoutParams = LinearLayout.LayoutParams(
                            resources.getDimensionPixelSize(R.dimen.color_indicator_size),
                            resources.getDimensionPixelSize(R.dimen.color_indicator_size)
                        )


                        // Membuat TextView untuk menampilkan nama kategori dengan bullet
                        val categoryTextView = TextView(this@StatistikActivity)
                        categoryTextView.text = "\u2022 ${entry.label}" // Karakter Unicode untuk bullet diikuti dengan nama kategori
                        categoryTextView.setTextColor(Color.BLACK) // Opsional: Mengatur warna teks
                        categoryTextView.setPadding(20, 0, 0, 0) // Opsional: Menambahkan padding untuk meninggalkan ruang di sisi kiri bullet


                        isiDatabaseKategori.addView(categoryTextView)
                    }
                }
            } else {
                // Jika tidak ada transaksi pada bulan yang dipilih, kosongkan chart
                val pieChart = findViewById<PieChart>(R.id.pieChart)
                pieChart.clear()
                pieChart.invalidate()
            }

        }


    }


    private fun showMonthPickerDialog() {
        Toast.makeText(this, "Fitur ini akan segera ditambahkan!", Toast.LENGTH_SHORT).show()
    }
}
