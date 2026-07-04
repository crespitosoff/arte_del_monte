package com.example.arte_del_monte.data.repository

import androidx.room.withTransaction
import com.example.arte_del_monte.data.db.AppDatabase
import com.example.arte_del_monte.data.db.dao.DocumentCounterDao
import com.example.arte_del_monte.data.db.dao.DocumentDao
import com.example.arte_del_monte.data.db.dao.DocumentItemDao
import com.example.arte_del_monte.data.db.entity.DocumentCounter
import com.example.arte_del_monte.data.db.entity.DocumentEntity
import com.example.arte_del_monte.data.db.entity.DocumentItemEntity
import com.example.arte_del_monte.data.model.DocumentType
import kotlinx.coroutines.flow.Flow

/**
 * Repository that orchestrates all document-related database operations.
 *
 * Accepts the individual DAOs plus [db] itself so that multi-step operations
 * can be wrapped in [AppDatabase.withTransaction] for atomicity.
 */
class DocumentRepository(
    private val db: AppDatabase,
    private val documentDao: DocumentDao,
    private val itemDao: DocumentItemDao,
    private val counterDao: DocumentCounterDao
) {

    // -------------------------------------------------------------------------
    // Flow getters (observed by ViewModels via StateFlow / collectAsState)
    // -------------------------------------------------------------------------

    /** Live-updating stream of all documents, newest first. */
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAll()

    /** Live-updating stream of documents of a specific [type], newest first. */
    fun getByType(type: DocumentType): Flow<List<DocumentEntity>> =
        documentDao.getByType(type.name)

    /** Live-updating stream of documents whose client name contains [name]. */
    fun searchByClient(name: String): Flow<List<DocumentEntity>> =
        documentDao.searchByClient(name)

    /** Live-updating stream of line items for a single document. */
    fun getItemsForDocument(documentId: Long): Flow<List<DocumentItemEntity>> =
        itemDao.getByDocumentId(documentId)

    // -------------------------------------------------------------------------
    // Suspend operations
    // -------------------------------------------------------------------------

    /**
     * Returns a [DocumentEntity] paired with its ordered list of [DocumentItemEntity].
     * Both reads happen outside a transaction because they are suspend reads;
     * callers that need strict consistency should call inside their own transaction.
     */
    suspend fun getDocumentWithItems(id: Long): Pair<DocumentEntity?, List<DocumentItemEntity>> {
        val doc = documentDao.getById(id)
        val items = if (doc != null) itemDao.getByDocumentIdOnce(id) else emptyList()
        return Pair(doc, items)
    }

    /**
     * Atomically saves (insert-or-replace) a document and replaces all its line items.
     *
     * Strategy:
     * 1. Insert/replace the document to get its stable ID.
     * 2. Delete all existing items for that document ID (handles edit scenarios).
     * 3. Re-insert the provided items, assigning the correct [documentId].
     *
     * @return the Row ID of the saved document.
     */
    suspend fun saveDocument(
        doc: DocumentEntity,
        items: List<DocumentItemEntity>
    ): Long = db.withTransaction {
        val savedId = documentDao.insert(doc)

        // Remove stale items before writing the new set
        itemDao.deleteByDocumentId(savedId)

        // Assign document ID and reset item IDs so Room treats them as new rows
        val reindexedItems = items.mapIndexed { index, item ->
            item.copy(
                id = 0L,
                documentId = savedId,
                sortOrder = index
            )
        }
        itemDao.insertAll(reindexedItems)

        savedId
    }

    /**
     * Atomically generates the next sequential number for [type] and returns it
     * as a formatted string, e.g. **"COT0001"** or **"REC0042"**.
     *
     * If no counter row exists yet for [type], one is created starting at 1.
     * The counter is incremented *before* reading so the returned value is always
     * the number that was just reserved.
     */
    suspend fun generateNextNumber(type: DocumentType): String = db.withTransaction {
        val existing = counterDao.getByType(type.name)
        if (existing == null) {
            // Bootstrap the counter for this document type at 1
            counterDao.insert(DocumentCounter(type = type.name, lastNumber = 1))
            "${type.prefix}${1.toString().padStart(4, '0')}"
        } else {
            counterDao.increment(type.name)
            val updated = counterDao.getByType(type.name)
                ?: error("Counter missing after increment for type ${type.name}")
            "${type.prefix}${updated.lastNumber.toString().padStart(4, '0')}"
        }
    }

    /**
     * Deletes a document by [id].
     * All associated [DocumentItemEntity] rows are removed automatically via the
     * `onDelete = CASCADE` foreign-key constraint defined on [DocumentItemEntity].
     */
    suspend fun deleteDocument(id: Long) {
        documentDao.deleteById(id)
    }
}
