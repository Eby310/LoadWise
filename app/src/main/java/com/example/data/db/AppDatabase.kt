package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Recharge
import com.example.data.model.RechargeConverters

@Database(entities = [Recharge::class], version = 1, exportSchema = false)
@TypeConverters(RechargeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rechargeDao(): RechargeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "loadwise.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
