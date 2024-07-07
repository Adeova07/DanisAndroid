package com.genus.backenddannis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.genus.backenddannis.data.dao.UserDao
import com.genus.backenddannis.data.dao.PendapatanDao
import com.genus.backenddannis.data.dao.PengeluaranDao
import com.genus.backenddannis.data.dao.KategoriDao
import com.genus.backenddannis.data.dao.TransaksiDao
import com.genus.backenddannis.data.entity.User
import com.genus.backenddannis.data.entity.Pendapatan
import com.genus.backenddannis.data.entity.Pengeluaran
import com.genus.backenddannis.data.entity.Kategori
import com.genus.backenddannis.data.entity.Transaksi

@Database(entities = [User::class, Pendapatan::class, Pengeluaran::class, Kategori::class, Transaksi::class], version = 7)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pendapatanDao(): PendapatanDao
    abstract fun pengeluaranDao(): PengeluaranDao
    abstract fun kategoriDao(): KategoriDao
    abstract fun transaksiDao(): TransaksiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    // Menambahkan migrasi dari versi 6 ke versi 7
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigrationOnDowngrade() // Opsional, menghindari penurunan versi yang tidak aman
                    .allowMainThreadQueries() // Mengizinkan query di main thread (sebaiknya dihindari di produksi)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        // Migrasi dari versi 6 ke versi 7
        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Implementasi migrasi di sini
            }
        }
    }
}
