package com.genus.backenddannis.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.genus.backenddannis.data.entity.Transaksi

@Dao
interface TransaksiDao {
    @Insert
    suspend fun insert(transaksi: Transaksi)

    @Query("SELECT * FROM transaksi_table")
    suspend fun getAllTransaksi(): List<Transaksi>

    // Mengambil transaksi berdasarkan ID
    @Query("SELECT * FROM transaksi_table WHERE id = :id")
    suspend fun getTransaksiById(id: Int): Transaksi?

}
