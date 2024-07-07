package com.genus.backenddannis

import android.content.Intent
import android.icu.text.NumberFormat
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.data.AppDatabase
import com.genus.backenddannis.data.adapter.CombineAdapter
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.Locale

class UtamaActivity : AppCompatActivity(), SwipeToDeleteHelper.OnSwipeListener {

    private lateinit var adapter: CombineAdapter
    private var balance = 0 // Mengubah tipe data balance menjadi Double


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utama)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val userNameTextView = findViewById<TextView>(R.id.tmpl_UserName)
        val balanceTextView = findViewById<TextView>(R.id.Balance)
        val eyeIcon = findViewById<ImageButton>(R.id.eyeicon)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        val menu2 = findViewById<LinearLayout>(R.id.menu2)
        val menu3 = findViewById<LinearLayout>(R.id.menu3)
        val menuTransaksiButton = findViewById<ImageButton>(R.id.menutransaksi)
        val recyclerViewMain = findViewById<RecyclerView>(R.id.recyclerViewMain)

        recyclerViewMain.layoutManager = LinearLayoutManager(this)

        val appDatabase = AppDatabase.getDatabase(this)

        // Menggunakan coroutine untuk melakukan operasi asinkron
        lifecycleScope.launch {
            val userDao = appDatabase.userDao()
            val users = userDao.getAll()

            // Jika ada pengguna, tampilkan nama pengguna pertama di TextView
            if (users.isNotEmpty()) {
                userNameTextView.text = users[0].userName
            }

            // Lanjutkan dengan mengambil data transaksi dan menyiapkannya di RecyclerView
            val pendapatanDao = appDatabase.pendapatanDao()
            val pengeluaranDao = appDatabase.pengeluaranDao()

            val pendapatans = pendapatanDao.getAll()
            val pengeluarans = pengeluaranDao.getAll()

            // Hitung total pendapatan
            var totalPendapatan = 0
            pendapatans.forEach { pendapatan ->
                totalPendapatan += pendapatan.jumlahP.toInt()
            }

            // Hitung total pengeluaran
            var totalPengeluaran = 0
            pengeluarans.forEach { pengeluaran ->
                totalPengeluaran += pengeluaran.jumlah.toInt()
            }


            // Hitung saldo
            balance = (totalPendapatan - totalPengeluaran)

            val formatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            balanceTextView.text = formatter.format(balance)

            // Tampilkan daftar transaksi di RecyclerView
            val transaksiList = mutableListOf<Any>()
            transaksiList.addAll(pendapatans)
            transaksiList.addAll(pengeluarans)

            val sortedTransaksiList = transaksiList.sortedByDescending { transaction ->
                when (transaction) {
                    is Pendapatan -> transaction.tanggalP
                    is Pengeluaran -> transaction.tanggal
                    else -> throw IllegalArgumentException("Invalid transaction type")
                }
            }

            adapter = CombineAdapter(sortedTransaksiList.toMutableList())
            recyclerViewMain.adapter = adapter


            val swipeToDeleteHelper = SwipeToDeleteHelper(adapter, lifecycleScope, pendapatanDao, pengeluaranDao, this@UtamaActivity)
            swipeToDeleteHelper.attachToRecyclerView(recyclerViewMain)
        }

        eyeIcon.setOnClickListener {
            // Toggle visibility of balance text
            val currentText = balanceTextView.text.toString()

            if (currentText.contains("Rp.")) {
                // Hide balance
                balanceTextView.text = "***.***,**"
            } else {
                // Show balance
                balanceTextView.text = "Rp. $balance" // Formatting balance as currency
            }
        }


        fabAdd.setOnClickListener {
            val intent = Intent(this@UtamaActivity, transaction_pengeluaran_activity::class.java)
            startActivity(intent)
        }

        menu2.setOnClickListener {
            val intent = Intent(this, StatistikActivity::class.java)
            startActivity(intent)
        }

        menu3.setOnClickListener {
            val intent = Intent(this, WalletMActivity::class.java)
            startActivity(intent)
        }

        menuTransaksiButton.setOnClickListener {
            val intent = Intent(this, RingkasanActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSwipe(position: Int) {
        adapter.removeItemAt(position)
    }
}
