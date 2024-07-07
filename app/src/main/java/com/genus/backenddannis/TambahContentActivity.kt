package com.genus.backenddannis

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.genus.backenddannis.data.AppDatabase
import com.genus.backenddannis.data.entity.Kategori

class TambahContentActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var addKategoriBaru: EditText
    private lateinit var simpanKategori: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_content)

        // Inisialisasi database
        db = AppDatabase.getDatabase(this)

        // Inisialisasi view
        addKategoriBaru = findViewById(R.id.AddKategoriBaru)
        simpanKategori = findViewById(R.id.SimpanKategori)

        // Menambahkan onClickListener pada tombol "SimpanKategori"
        simpanKategori.setOnClickListener {
            // Ambil teks dari TextInputEditText
            val kategoriText = addKategoriBaru.text.toString().trim()

            // Pastikan teks yang dimasukkan tidak kosong
            if (kategoriText.isNotEmpty()) {
                // Buat objek Kategori
                val kategori = Kategori(KategoriAdd = kategoriText)

                // Masukkan data Kategori ke dalam database
                db.kategoriDao().insertAll(kategori)

                // Kembali ke WalletMActivity setelah berhasil menyimpan
                val intent = Intent(this, WalletMActivity::class.java)
                startActivity(intent)
                finish() // Menutup TambahContentActivity agar tidak kembali lagi jika tombol back ditekan
            } else {
                // Tampilkan dialog peringatan untuk meminta pengguna mengisi kategori
                showEmptyInputAlert()
            }
        }

    }

    // Fungsi untuk menampilkan dialog peringatan jika input kosong
    private fun showEmptyInputAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Peringatan")
            .setMessage("Kategori tidak boleh kosong!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
