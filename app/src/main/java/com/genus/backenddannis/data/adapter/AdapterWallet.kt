package com.genus.backenddannis.data.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.genus.backenddannis.DetailSaldo
import com.genus.backenddannis.R
import com.genus.backenddannis.TambahContentActivity
import com.genus.backenddannis.data.dao.KategoriDao
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import com.genus.backenddannis.data.toRupiahFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

class AdapterWallet(
    private val context: Context,
    kategoriDao: KategoriDao, // Tambahkan kategoriDao sebagai parameter
    private val pendapatanList: List<Pendapatan>,
    private val pengeluaranList: List<Pengeluaran>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var kategoriDompetList: List<String> = emptyList()
    private var uniqueKategoriDompetList: List<String> = emptyList()

    init {
        // Mengambil kategori dari database dan menggabungkannya dengan kategori dari pendapatan dan pengeluaran
        CoroutineScope(Dispatchers.IO).launch {
            val kategoriFromDb = kategoriDao.getKategoriAddList()
            uniqueKategoriDompetList = (kategoriFromDb + pendapatanList.map { it.kategoriDompet } + pengeluaranList.map { it.kategoriDompet }).distinct()
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_KATEGORI = 0
        private const val VIEW_TYPE_ADDCd = 1
    }

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val kategoriDompetTextView: TextView = itemView.findViewById(R.id.KategoriDompet)
        val saldoKategoriTextView: TextView = itemView.findViewById(R.id.saldoKategori)
    }

    class AddCdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addKategoriButton: ImageButton = itemView.findViewById(R.id.addKategori)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_KATEGORI) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.tampilan_dompet, parent, false)
            WalletViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.tampilan_add, parent, false)
            AddCdViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_KATEGORI) {
            val walletHolder = holder as WalletViewHolder
            val kategoriDompet = uniqueKategoriDompetList.getOrNull(position) ?: return

            walletHolder.kategoriDompetTextView.text = kategoriDompet

            val totalPendapatan = pendapatanList.filter { it.kategoriDompet == kategoriDompet }
                .sumOf { it.jumlahP }
            val totalPengeluaran = pengeluaranList.filter { it.kategoriDompet == kategoriDompet }
                .sumOf { it.jumlah }

            val saldo = totalPendapatan - totalPengeluaran
            val formattedSaldo = if (saldo != 0.0) saldo.toRupiahFormat() else "0"
            walletHolder.saldoKategoriTextView.text = "Rp. $formattedSaldo"

            // Menambahkan onClickListener untuk CdContent
            walletHolder.itemView.findViewById<View>(R.id.CdContent).setOnClickListener {
                val intent = Intent(context, DetailSaldo::class.java).apply {
                    putExtra("KATEGORI_DOMPET", kategoriDompet)
                    putExtra("SALDO", formattedSaldo)
                    putExtra("PENDAPATAN_LIST", ArrayList(pendapatanList.filter { it.kategoriDompet == kategoriDompet }) as Serializable)
                    putExtra("PENGELUARAN_LIST", ArrayList(pengeluaranList.filter { it.kategoriDompet == kategoriDompet }) as Serializable)
                }
                context.startActivity(intent)
            }
        } else {
            val addCdHolder = holder as AddCdViewHolder
            addCdHolder.addKategoriButton.setOnClickListener {
                val intent = Intent(context, TambahContentActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return uniqueKategoriDompetList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < uniqueKategoriDompetList.size) VIEW_TYPE_KATEGORI else VIEW_TYPE_ADDCd
    }
}
