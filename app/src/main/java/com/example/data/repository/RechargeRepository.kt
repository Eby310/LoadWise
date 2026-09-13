package com.example.data.repository

import com.example.data.db.RechargeDao
import com.example.data.model.Recharge
import kotlinx.coroutines.flow.Flow

class RechargeRepository(private val rechargeDao: RechargeDao) {
    val allRecharges: Flow<List<Recharge>> = rechargeDao.getAllRecharges()

    suspend fun insert(recharge: Recharge): Long = rechargeDao.insertRecharge(recharge)

    suspend fun update(recharge: Recharge) = rechargeDao.updateRecharge(recharge)

    suspend fun delete(recharge: Recharge) = rechargeDao.deleteRecharge(recharge)

    suspend fun deleteById(id: Long) = rechargeDao.deleteRechargeById(id)

    suspend fun getMonthlySpend(startOfMonth: Long, endOfMonth: Long): Double =
        rechargeDao.getMonthlySpend(startOfMonth, endOfMonth) ?: 0.0

    suspend fun clearAll() = rechargeDao.clearAll()
}
