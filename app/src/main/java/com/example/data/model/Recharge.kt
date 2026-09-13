package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "recharges")
data class Recharge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: RechargeType,
    val network: String,
    val amount: Double,
    val dataSizeMb: Int? = null,
    val date: Long,
    val note: String? = null
)

class RechargeConverters {
    @TypeConverter
    fun fromRechargeType(type: RechargeType): String = type.name

    @TypeConverter
    fun toRechargeType(value: String): RechargeType = runCatching {
        RechargeType.valueOf(value)
    }.getOrDefault(RechargeType.DATA)
}
