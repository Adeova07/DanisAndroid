package com.genus.backenddannis.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transaksi_table")
data class Transaksi(
    val tanggal: String,
    val jumlah: String,
    val kategoriDari: String,
    var kategoriKepada: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
