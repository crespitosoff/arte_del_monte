package com.example.arte_del_monte.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arte_del_monte.data.db.entity.DocumentCounter

@Dao
interface DocumentCounterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(counter: DocumentCounter)

    @Query("SELECT * FROM document_counters WHERE type = :type")
    suspend fun getByType(type: String): DocumentCounter?

    @Query("UPDATE document_counters SET lastNumber = lastNumber + 1 WHERE type = :type")
    suspend fun increment(type: String)
}
