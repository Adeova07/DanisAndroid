package com.genus.backenddannis.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kategori_table")
data class Kategori(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val KategoriAdd: String
)
