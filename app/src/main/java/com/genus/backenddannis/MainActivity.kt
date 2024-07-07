package com.genus.backenddannis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.genus.backenddannis.data.AppDatabase
import com.genus.backenddannis.data.entity.User
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Memeriksa apakah ada pengguna yang sudah login
        val userDao = AppDatabase.getDatabase(this).userDao()
        val user = userDao.getAll().firstOrNull()

        // Jika sudah ada pengguna yang login, langsung menuju UtamaActivity
        if (user != null && user.uid != null && user.uid!! > 0) {
            startUtamaActivity()
            return
        }

        setContentView(R.layout.activity_main)

        val btnSave = findViewById<Button>(R.id.btn_save)
        val textInputLayout = findViewById<TextInputLayout>(R.id.User_Name)
        val editText = textInputLayout.editText

        btnSave.setOnClickListener {
            val userName = editText?.text.toString().trim()

            if (userName.isNotEmpty()) {
                lifecycleScope.launch {
                    saveUserToDatabase(userName)
                    startUtamaActivity()
                }
            } else {
                Toast.makeText(this@MainActivity, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun saveUserToDatabase(userName: String) {
        withContext(Dispatchers.IO) {
            val userDao = AppDatabase.getDatabase(this@MainActivity).userDao()
            val user = User(userName = userName)
            userDao.insertAll(user)
        }
    }

    private fun startUtamaActivity() {
        val intent = Intent(this, UtamaActivity::class.java)
        startActivity(intent)
        finish() // Menutup MainActivity agar tidak dapat kembali dengan menekan tombol back
    }
}
