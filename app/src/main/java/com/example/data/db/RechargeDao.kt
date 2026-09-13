package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Recharge
import kotlinx.coroutines.flow.Flow

@Dao
interface RechargeDao {
    @Query("SELECT * FROM recharges ORDER BY date DESC")
    fun getAllRecharges(): Flow<List<Recharge>>

    @Query("SELECT * FROM recharges WHERE id = :id LIMIT 1")
    suspend fun getRechargeById(id: Long): Recharge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecharge(recharge: Recharge): Long

    @Update
    suspend fun updateRecharge(recharge: Recharge)

    @Delete
    suspend fun deleteRecharge(recharge: Recharge)

    @Query("DELETE FROM recharges WHERE id = :id")
    suspend fun deleteRechargeById(id: Long)

    @Query("SELECT SUM(amount) FROM recharges WHERE date >= :startOfMonth AND date <= :endOfMonth")
    suspend fun getMonthlySpend(startOfMonth: Long, endOfMonth: Long): Double?

    @Query("DELETE FROM recharges")
    suspend fun clearAll()
}
