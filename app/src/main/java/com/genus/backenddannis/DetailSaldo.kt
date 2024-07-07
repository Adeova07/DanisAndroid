package com.genus.backenddannis

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.data.adapter.CombineAdapter
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran

class DetailSaldo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_saldo)

        val kategoriDompet = intent.getStringExtra("KATEGORI_DOMPET")
        val saldo = intent.getStringExtra("SALDO")

        val kategoriDompetTextView: TextView = findViewById(R.id.kategoriDompetTextView)
        val saldoTextView: TextView = findViewById(R.id.saldoTextView)
        val recyclerView: RecyclerView = findViewById(R.id.recvDetail)

        kategoriDompetTextView.text = kategoriDompet ?: "Tidak ada kategori"
        saldoTextView.text = "Rp. ${saldo ?: "0"}"

        // Terima data pendapatan dan pengeluaran dari Intent
        val pendapatanList = intent.getSerializableExtra("PENDAPATAN_LIST") as List<Pendapatan>
        val pengeluaranList = intent.getSerializableExtra("PENGELUARAN_LIST") as List<Pengeluaran>

        // Gabungkan pendapatan dan pengeluaran ke dalam satu daftar transaksi
        val transactionList: MutableList<Any> = mutableListOf()
        transactionList.addAll(pendapatanList)
        transactionList.addAll(pengeluaranList)

        val adapter = CombineAdapter(transactionList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val back = findViewById<ImageButton>(R.id.backtostatistik)
        back.setOnClickListener{
            val kembali = Intent(this, WalletMActivity :: class.java)
            startActivities(arrayOf(kembali))
        }
    }
}
