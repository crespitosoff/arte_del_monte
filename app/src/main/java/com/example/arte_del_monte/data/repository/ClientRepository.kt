package com.example.arte_del_monte.data.repository

import com.example.arte_del_monte.data.db.dao.ClientDao
import com.example.arte_del_monte.data.db.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository that abstracts access to [ClientDao].
 * All database interactions for clients flow through here.
 */
class ClientRepository(private val dao: ClientDao) {

    /** Live-updating list of all clients, ordered A-Z by name. */
    val allClients: Flow<List<ClientEntity>> = dao.getAll()

    /**
     * Returns a live-updating list of clients whose name contains [query] (case-insensitive
     * due to SQLite's default LIKE behaviour).
     */
    fun search(query: String): Flow<List<ClientEntity>> = dao.search(query)

    /**
     * Inserts or replaces a client record.
     * @return the row ID of the inserted client.
     */
    suspend fun save(client: ClientEntity): Long = dao.insert(client)

    /** Updates an existing client record. */
    suspend fun update(client: ClientEntity) = dao.update(client)

    /** Deletes a client record. */
    suspend fun delete(client: ClientEntity) = dao.delete(client)

    /** Returns a single client by [id], or null if not found. */
    suspend fun getById(id: Long): ClientEntity? = dao.getById(id)
}
