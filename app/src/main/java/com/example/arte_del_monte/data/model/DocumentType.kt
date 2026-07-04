package com.example.arte_del_monte.data.model

enum class DocumentType(val prefix: String, val label: String) {
    COTIZACION("COT", "Cotización"),
    RECIBO_PAGO("REC", "Recibo de pago")
}
