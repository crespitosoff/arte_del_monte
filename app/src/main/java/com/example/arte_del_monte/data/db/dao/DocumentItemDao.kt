package com.example.arte_del_monte.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.arte_del_monte.data.db.entity.DocumentItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DocumentItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DocumentItemEntity>)

    @Update
    suspend fun update(item: DocumentItemEntity)

    @Query("DELETE FROM document_items WHERE documentId = :documentId")
    suspend fun deleteByDocumentId(documentId: Long)

    @Query("SELECT * FROM document_items WHERE documentId = :documentId ORDER BY sortOrder ASC")
    fun getByDocumentId(documentId: Long): Flow<List<DocumentItemEntity>>

    @Query("SELECT * FROM document_items WHERE documentId = :documentId ORDER BY sortOrder ASC")
    suspend fun getByDocumentIdOnce(documentId: Long): List<DocumentItemEntity>
}
