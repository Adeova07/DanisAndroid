package com.genus.backenddannis
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.genus.backenddannis.data.AppDatabase
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import com.genus.backenddannis.data.entity.Transaksi
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TransferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        val tanggalEditText = findViewById<TextInputEditText>(R.id.tanggalID)
        val jamEditText = findViewById<TextInputEditText>(R.id.jamID)
        val jumlahEditText = findViewById<TextInputEditText>(R.id.jumlahID)
        val kategori1Spinner = findViewById<Spinner>(R.id.kategori1_Id)
        val kategori2Spinner = findViewById<Spinner>(R.id.kategori2_id)
        val simpanButton = findViewById<Button>(R.id.SimpanPendapatan)

        // Ambil data kategori dari database
        val database = AppDatabase.getDatabase(applicationContext)
        val kategoriDao = database.kategoriDao()
        val kategoriList = kategoriDao.getKategoriAddList()

        // Inisialisasi adapter untuk spinner kategori
        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriList)
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set adapter ke spinner kategori
        kategori1Spinner.adapter = kategoriAdapter
        kategori2Spinner.adapter = kategoriAdapter

        // Mendapatkan tanggal saat ini dalam format yang diinginkan
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        tanggalEditText.setText(currentDate)
        jamEditText.setText(currentTime)

        // Setup listener untuk TextInputEditText tanggalID
        tanggalEditText.setOnClickListener {
            showDatePickerDialog(tanggalEditText)
        }

        // Setup listener untuk TextInputEditText jamID
        jamEditText.setOnClickListener {
            showTimePickerDialog(jamEditText)
        }

        // Setup listener untuk tombol Simpan
        simpanButton.setOnClickListener {
            val tanggal = tanggalEditText.text.toString()
            val jam = jamEditText.text.toString()
            val jumlah = jumlahEditText.text.toString()
            val kategori1 = kategori1Spinner.selectedItem.toString()
            val kategori2 = kategori2Spinner.selectedItem.toString()

            // Validasi input
            if (tanggal.isEmpty() || jam.isEmpty() || jumlah.isEmpty() || kategori1.isEmpty() || kategori2.isEmpty()) {
                Toast.makeText(this, "Harap lengkapi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gabungkan tanggal dan jam menjadi satu string
            val dateTime = "$tanggal $jam"

            // Simpan transaksi ke database
            val transaksi = Transaksi(dateTime, jumlah, kategori1, kategori2)
            saveTransaksiToDatabase(transaksi)
        }
    }

    private fun showDatePickerDialog(textInputEditText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
                textInputEditText.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(textInputEditText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)
                textInputEditText.setText(formattedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-hour format
        )
        timePickerDialog.show()
    }

    private fun saveTransaksiToDatabase(transaksi: Transaksi) {
        GlobalScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(applicationContext)
            val transaksiDao = database.transaksiDao()
            val pendapatanDao = database.pendapatanDao()
            val pengeluaranDao = database.pengeluaranDao()

            // Mengonversi tanggal string menjadi objek Date
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val tanggalDate = formatter.parse(transaksi.tanggal)

            // Validasi bahwa kategoriDari dan kategoriKepada tidak sama
            if (transaksi.kategoriDari == transaksi.kategoriKepada) {
                // Jika kategoriDari dan kategoriKepada sama, cari kategori yang berbeda
                val kategoriDari = transaksi.kategoriDari
                val kategoriBaru = pendapatanDao.getAll().find { it.kategoriDompet != kategoriDari }?.kategoriDompet
                if (kategoriBaru != null) {
                    transaksi.kategoriKepada = kategoriBaru
                } else {
                    // Tampilkan pesan kesalahan jika tidak ada kategori yang berbeda
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TransferActivity, "Tidak dapat menemukan kategori yang berbeda", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
            }

            // Buat pendapatan baru untuk transaksi ini
            val pendapatanBaru = Pendapatan(
                tanggalP = tanggalDate,
                jumlahP = transaksi.jumlah.toDouble(),
                kategoriDompet = transaksi.kategoriKepada,
                deskripsiP = transaksi.kategoriKepada, // Deskripsi default
                kategoriP = transaksi.kategoriDari // Kategori default
            )

            // Buat pengeluaran baru untuk transaksi ini
            val pengeluaranBaru = Pengeluaran(
                tanggal = tanggalDate,
                jumlah = transaksi.jumlah.toDouble(), // Ubah ke negatif
                deskripsi = transaksi.kategoriDari, // Deskripsi default
                kategori = transaksi.kategoriKepada, // Kategori default
                kategoriDompet = transaksi.kategoriDari // Menggunakan kategoriKepada dari transaksi
            )

            // Simpan pendapatan baru ke database
            pendapatanDao.insert(pendapatanBaru)

            // Simpan pengeluaran baru ke database
            pengeluaranDao.insert(pengeluaranBaru)

            // Simpan transaksi ke database
            transaksiDao.insert(transaksi)

            // Kembali ke WalletMActivity setelah berhasil menyimpan transaksi
            withContext(Dispatchers.Main) {
                val intent = Intent(this@TransferActivity, WalletMActivity::class.java)
                startActivity(intent)
                finish() // Selesai aktivitas TransferActivity agar tidak kembali ke sini saat menekan tombol back
            }
        }
    }
}
