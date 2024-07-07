package com.genus.backenddannis

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.genus.backenddannis.data.AppDatabase
import com.genus.backenddannis.data.entity.Pengeluaran
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class transaction_pengeluaran_activity : AppCompatActivity() {
    private lateinit var tanggalID: TextInputEditText
    private lateinit var jamID: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_pengeluaran)

        // Daftar kategori
        val kategoriList = arrayOf(
            "Utilitas",
            "Pakaian",
            "Pendidikan",
            "Hiburan",
            "Kebugaran",
            "Makan",
            "Cicilan",
            "Hutang",
            "Pengeluaran Tidak Terduga"
        )

        // Adapter untuk Spinner kategori
        val adapterKategori = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            kategoriList
        )
        adapterKategori.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Ambil daftar kategori dompet dari database (contoh: menggunakan metode dari AppDatabase)
        val dompet = AppDatabase.getDatabase(this).kategoriDao().getKategoriAddList()

        // Buat adapter untuk Spinner dompet dengan menggunakan daftar kategori dompet yang telah Anda ambil
        val adapterDompet = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            dompet
        )
        adapterDompet.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Mengakses Spinner dengan findViewById
        val spinnerKategori = findViewById<Spinner>(R.id.kategoriID)
        val spinnerDompet = findViewById<Spinner>(R.id.dompetID)

        // Set adapter ke Spinner
        spinnerKategori.adapter = adapterKategori
        spinnerDompet.adapter = adapterDompet

        val jumlahEditText = findViewById<EditText>(R.id.jumlahID)
        jumlahEditText.addTextChangedListener(NumberTextWatcher(jumlahEditText))

        val btnPengeluaran = findViewById<Button>(R.id.btn_pengeluaran)
        val color = ContextCompat.getColor(this, R.color.menu_icon_color)
        btnPengeluaran.setBackgroundColor(color)

        tanggalID = findViewById(R.id.tanggalID)
        jamID = findViewById(R.id.jamID)

        // Inisialisasi DatePickerDialog
        tanggalID.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    tanggalID.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Inisialisasi TimePickerDialog
        jamID.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedTime.set(Calendar.MINUTE, minute)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    jamID.setText(timeFormat.format(selectedTime.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        val btnPendapatan = findViewById<Button>(R.id.btn_pendapatan)
        btnPendapatan.setOnClickListener {
            val intent = Intent(this, transaction_pendapatan_activity::class.java)
            startActivity(intent)
        }

        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        btnClose.setOnClickListener {
            Log.d("ImageButton", "Button clicked")
            val intent = Intent(this, UtamaActivity::class.java)
            startActivity(intent)
            btnClose.setImageResource(R.drawable.id_close_red)
        }

        val btnSimpanPengeluaran = findViewById<Button>(R.id.SimpanPengeluaran)
        btnSimpanPengeluaran.setOnClickListener {
            val tanggal = tanggalID.text.toString()
            val jam = jamID.text.toString()
            val jumlah = findViewById<EditText>(R.id.jumlahID).text.toString()
            val deskripsi = findViewById<EditText>(R.id.deskripsiID).text.toString()
            val kategori = spinnerKategori.selectedItem.toString()
            val kategoriDompet = spinnerDompet.selectedItem.toString()

            val dateTime = "$tanggal $jam"

            lifecycleScope.launch {
                try {
                    savePengeluaranToDatabase(dateTime, jumlah, deskripsi, kategori, kategoriDompet)
                } catch (e: ParseException) {
                    Log.e("ParseException", "Error parsing date", e)
                }
                val intent = Intent(this@transaction_pengeluaran_activity, UtamaActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    class NumberTextWatcher(val editText: EditText) : TextWatcher {
        private val decimalFormat = DecimalFormat("#,###")

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            editText.removeTextChangedListener(this)

            val cleanString = s.toString().replace("[,.]".toRegex(), "")

            try {
                val parsed = decimalFormat.parse(cleanString)
                val formatted = decimalFormat.format(parsed)

                editText.setText(if (formatted.isNotEmpty()) formatted else "")
                editText.setSelection(editText.text.length)
            } catch (ex: ParseException) {
                ex.printStackTrace()
            }

            editText.addTextChangedListener(this)
        }
    }

    private suspend fun savePengeluaranToDatabase(
        dateTime: String,
        jumlah: String,
        deskripsi: String,
        kategori: String,
        kategoriDompet: String
    ) {
        withContext(Dispatchers.IO) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val date = dateFormat.parse(dateTime)

            val jumlahDouble = jumlah.replace("[,.]".toRegex(), "").toDoubleOrNull() ?: 0.0

            val pengeluaran = Pengeluaran(
                tanggal = date,
                jumlah = jumlahDouble,
                deskripsi = deskripsi,
                kategori = kategori,
                kategoriDompet = kategoriDompet
            )

            val pengeluaranDao = AppDatabase.getDatabase(this@transaction_pengeluaran_activity).pengeluaranDao()
            pengeluaranDao.insert(pengeluaran)
        }
    }
}

