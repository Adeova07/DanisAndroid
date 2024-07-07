package com.genus.backenddannis.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.genus.backenddannis.data.entity.Pendapatan
import java.util.Date

@Dao
interface PendapatanDao {
    @Query("SELECT * FROM pendapatan_table WHERE strftime('%m', tanggalPendapatan) = :bulan AND strftime('%Y', tanggalPendapatan) = :tahun")
    fun getAllByMonthAndYear(bulan: String, tahun: String): List<Pendapatan>

    @Query("SELECT * FROM pendapatan_table")
    fun getAll(): List<Pendapatan>

    @Query("SELECT * FROM pendapatan_table WHERE tanggalPendapatan BETWEEN :startDate AND :endDate")
    fun getAllByDateRange(startDate: Date, endDate: Date): List<Pendapatan>

    @Query("SELECT * FROM pendapatan_table WHERE kategoriDompet = :dompet")
    fun getAllByKategoriDompet(dompet: String): List<Pendapatan>

    @Insert
    fun insert(vararg pendapatan: Pendapatan)

    @Delete
    fun delete(pendapatan: Pendapatan)

    @Update
    fun updatePendapatan(pendapatan: Pendapatan) // Perubahan tipe parameter dari vararg menjadi Pendapatan
}
