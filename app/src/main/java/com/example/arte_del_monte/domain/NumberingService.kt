package com.example.arte_del_monte.domain

import com.example.arte_del_monte.data.db.dao.DocumentCounterDao
import com.example.arte_del_monte.data.db.entity.DocumentCounter
import com.example.arte_del_monte.data.model.DocumentType

class NumberingService(private val counterDao: DocumentCounterDao) {

    /**
     * Genera el próximo número de documento de forma atómica.
     * Ejemplo: COTIZACION con lastNumber=0 → "COT0001"
     */
    suspend fun next(type: DocumentType): String {
        val existing = counterDao.getByType(type.name)
        return if (existing == null) {
            counterDao.insert(DocumentCounter(type = type.name, lastNumber = 1))
            formatNumber(type, 1)
        } else {
            counterDao.increment(type.name)
            formatNumber(type, existing.lastNumber + 1)
        }
    }

    private fun formatNumber(type: DocumentType, n: Int): String =
        type.prefix + n.toString().padStart(4, '0')
}
