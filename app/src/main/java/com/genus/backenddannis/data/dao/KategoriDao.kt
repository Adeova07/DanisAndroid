package com.genus.backenddannis.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.genus.backenddannis.data.entity.Kategori

@Dao
interface KategoriDao {
    @Query("SELECT * FROM kategori_table")
    fun getAll(): List<Kategori>

    @Query("SELECT * FROM kategori_table WHERE id = :id")
    fun getById(id: Int): Kategori?

    @Query("SELECT KategoriAdd FROM kategori_table")
    fun getKategoriAddList(): List<String> // Metode untuk mengambil daftar nama kategori

    @Insert
    fun insertAll(vararg kategori: Kategori)

    @Delete
    fun delete(kategori: Kategori)
}

