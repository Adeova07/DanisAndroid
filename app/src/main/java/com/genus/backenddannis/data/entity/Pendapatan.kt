package com.genus.backenddannis.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "pendapatan_table")
data class Pendapatan(
    @PrimaryKey(autoGenerate = true) var pid: Int? = null,
    @ColumnInfo(name = "tanggalPendapatan") var tanggalP: Date?,
    @ColumnInfo(name = "jumlahPendapatan") val jumlahP: Double,
    @ColumnInfo(name = "deskripsiPendapatan") val deskripsiP: String,
    @ColumnInfo(name = "kategoriPendapatan") val kategoriP: String,
    @ColumnInfo(name = "kategoriDompet") val kategoriDompet: String
) : Serializable
