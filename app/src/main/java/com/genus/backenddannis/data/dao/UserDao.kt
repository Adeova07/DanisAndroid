package com.genus.backenddannis.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.genus.backenddannis.data.entity.User

@Dao
interface UserDao {

    @Query("SELECT * FROM User")
    fun getAll(): List<User>

    @Query("SELECT * FROM User WHERE uid = :userId")
    suspend fun getUserById(userId: Int): User?

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}
