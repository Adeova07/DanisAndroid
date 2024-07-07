package com.genus.backenddannis.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "pengeluaran_table")
data class Pengeluaran(
    @PrimaryKey(autoGenerate = true) var pid: Int? = null,
    @ColumnInfo(name = "tanggal") var tanggal: Date?,
    @ColumnInfo(name = "jumlah") val jumlah: Double,
    @ColumnInfo(name = "deskripsi") val deskripsi: String,
    @ColumnInfo(name = "kategori") val kategori: String,
    @ColumnInfo(name = "kategori_dompet") val kategoriDompet: String
) : Serializable
