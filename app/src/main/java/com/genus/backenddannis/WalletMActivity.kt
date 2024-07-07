package com.genus.backenddannis

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.data.AppDatabase
import com.genus.backenddannis.data.adapter.AdapterWallet
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletMActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_mactivity)

        // Inisialisasi RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerDompet)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inisialisasi AppDatabase
        val appDatabase = AppDatabase.getDatabase(this)

        // Menambahkan listener onClick pada menu1
        val menu1 = findViewById<LinearLayout>(R.id.menu1)
        menu1.setOnClickListener {
            // Membuat intent untuk memulai UtamaActivity
            val intent = Intent(this, UtamaActivity::class.java)
            startActivity(intent)
        }

        // Menambahkan listener onClick pada menu2
        val menu2 = findViewById<LinearLayout>(R.id.menu2)
        menu2.setOnClickListener {
            // Membuat intent untuk memulai StatistikActivity
            val intent = Intent(this, StatistikActivity::class.java)
            startActivity(intent)
        }

        // Menambahkan FabButton
        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add_dompet)
        fabAdd.setOnClickListener {
            val intent = Intent(this, TransferActivity::class.java)
            startActivity(intent)
        }

        // Mengambil data dari database secara asinkron
        CoroutineScope(Dispatchers.IO).launch {
            val pendapatanList: List<Pendapatan> = appDatabase.pendapatanDao().getAll()
            val pengeluaranList: List<Pengeluaran> = appDatabase.pengeluaranDao().getAll()
            val kategoriDao = appDatabase.kategoriDao()

            withContext(Dispatchers.Main) {
                // Inisialisasi AdapterWallet dan atur RecyclerView dengan adapter
                val adapter = AdapterWallet(this@WalletMActivity, kategoriDao, pendapatanList, pengeluaranList)
                recyclerView.adapter = adapter
            }
        }
    }
}
