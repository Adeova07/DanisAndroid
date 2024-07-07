package com.genus.backenddannis.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.genus.backenddannis.data.entity.Pengeluaran
import java.util.Date

@Dao
interface PengeluaranDao {

    @Query("SELECT * FROM pengeluaran_table WHERE strftime('%m', tanggal) = :bulan AND strftime('%Y', tanggal) = :tahun")
    fun getAllByMonthAndYear(bulan: String, tahun: String): List<Pengeluaran>

    @Query("SELECT * FROM pengeluaran_table")
    fun getAll(): List<Pengeluaran>

    @Query("SELECT * FROM pengeluaran_table WHERE tanggal BETWEEN :startDate AND :endDate")
    fun getAllByDateRange(startDate: Date, endDate: Date): List<Pengeluaran>

    @Query("SELECT * FROM pengeluaran_table WHERE kategori_dompet = :dompet")
    fun getAllByDompet(dompet: String): List<Pengeluaran>

    @Insert
    fun insert(vararg pengeluaran: Pengeluaran)

    @Delete
    fun delete(pengeluaran: Pengeluaran)
}
