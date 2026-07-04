package com.example.arte_del_monte.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.arte_del_monte.data.db.dao.ClientDao
import com.example.arte_del_monte.data.db.dao.DocumentCounterDao
import com.example.arte_del_monte.data.db.dao.DocumentDao
import com.example.arte_del_monte.data.db.dao.DocumentItemDao
import com.example.arte_del_monte.data.db.entity.ClientEntity
import com.example.arte_del_monte.data.db.entity.DocumentCounter
import com.example.arte_del_monte.data.db.entity.DocumentEntity
import com.example.arte_del_monte.data.db.entity.DocumentItemEntity

@Database(
    entities = [
        ClientEntity::class,
        DocumentEntity::class,
        DocumentItemEntity::class,
        DocumentCounter::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun documentDao(): DocumentDao
    abstract fun documentItemDao(): DocumentItemDao
    abstract fun documentCounterDao(): DocumentCounterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arte_del_monte.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
