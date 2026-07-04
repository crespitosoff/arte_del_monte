package com.example.arte_del_monte.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arte_del_monte.data.model.DocumentType
import com.example.arte_del_monte.domain.PricingCalculator
import com.example.arte_del_monte.ui.components.PhotoPickerRow
import com.example.arte_del_monte.ui.theme.*
import com.example.arte_del_monte.ui.viewmodel.DocumentEditorViewModel
import com.example.arte_del_monte.ui.viewmodel.ItemUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditorScreen(
    documentId: Long = 0L,
    initialType: DocumentType = DocumentType.COTIZACION,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val vm: DocumentEditorViewModel = viewModel(factory = DocumentEditorViewModel.Factory(context))
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(documentId, initialType) {
        if (documentId != 0L) vm.loadDocument(documentId)
        else vm.initNew(initialType)
    }

    LaunchedEffect(state.savedDocumentId) {
        state.savedDocumentId?.let { onSaved(it) }
    }

    Scaffold(
        containerColor = CremaPapelLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (documentId == 0L) "Nuevo documento" else "Editar ${state.documentNumber}",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { vm.save(onSaved) },
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving)
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else
                            Icon(Icons.Default.Save, "Guardar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CremaPapel,
                    titleContentColor = RobleOscuro,
                    actionIconContentColor = CafeMonte,
                    navigationIconContentColor = CafeMonte
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EditorCard("Tipo y número") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DocumentType.entries.forEach { type ->
                            FilterChip(
                                selected = state.documentType == type,
                                onClick = { if (documentId == 0L) vm.setType(type) },
                                label = { Text(type.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CafeMonte,
                                    selectedLabelColor = CremaPapel
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("N° ${state.documentNumber}", style = MaterialTheme.typography.titleMedium, color = RobleOscuro)
                        Text(
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(state.date)),
                            style = MaterialTheme.typography.bodyMedium, color = CafeMonte
                        )
                    }
                }
            }

            item {
                EditorCard("Cliente") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = state.clientName,
                            onValueChange = vm::setClientName,
                            label = { Text("Nombre del cliente *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.clientPhone,
                            onValueChange = vm::setClientPhone,
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = state.clientAddress,
                            onValueChange = vm::setClientAddress,
                            label = { Text("Dirección (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ítems / Trabajos", style = MaterialTheme.typography.titleMedium, color = RobleOscuro)
                    FilledTonalButton(onClick = vm::addItem) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar ítem")
                    }
                }
            }

            itemsIndexed(state.items) { index, item ->
                ItemEditorCard(
                    item = item,
                    index = index,
                    onUpdate = vm::updateItem,
                    onRemove = { vm.removeItem(item.id) },
                    onAddPhoto = { path -> vm.addPhoto(item.id, path) },
                    onRemovePhoto = { path -> vm.removePhoto(item.id, path) }
                )
            }

            if (state.items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Agrega al menos un ítem al documento",
                            color = CafeMonte,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                EditorCard("Totales") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = if (state.globalDiscountPercent == 0.0) "" else state.globalDiscountPercent.toString(),
                            onValueChange = { vm.setGlobalDiscount(it.toDoubleOrNull() ?: 0.0) },
                            label = { Text("Descuento global (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Aplicar impuesto", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = state.taxEnabled, onCheckedChange = vm::setTaxEnabled)
                        }
                        if (state.taxEnabled) {
                            OutlinedTextField(
                                value = if (state.taxRate == 0.0) "" else state.taxRate.toString(),
                                onValueChange = { vm.setTaxRate(it.toDoubleOrNull() ?: 0.0) },
                                label = { Text("Porcentaje de impuesto (%)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        TotalRow("Subtotal", PricingCalculator.formatCurrency(state.subtotal))
                        if (state.taxEnabled && state.taxAmount > 0)
                            TotalRow("Impuesto (${state.taxRate}%)", PricingCalculator.formatCurrency(state.taxAmount))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NegroMate, RoundedCornerShape(10.dp))
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "TOTAL",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CremaPapel,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    PricingCalculator.formatCurrency(state.total),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = CremaPapel,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            item {
                EditorCard("Condiciones y observaciones") {
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = vm::setNotes,
                        label = { Text("Tiempo de entrega, forma de pago, garantía...") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        minLines = 3
                    )
                }
            }

            item {
                Button(
                    onClick = { vm.save(onSaved) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeMonte, contentColor = CremaPapel)
                ) {
                    if (state.isSaving)
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CremaPapel)
                    else {
                        Icon(Icons.Default.Visibility, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar y ver documento")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemEditorCard(
    item: ItemUiState,
    index: Int,
    onUpdate: (ItemUiState) -> Unit,
    onRemove: () -> Unit,
    onAddPhoto: (String) -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val subtotal = PricingCalculator.lineSubtotal(item.quantity, item.unitPrice, item.discountPercent)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CremaPapel),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ítem ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    color = CafeMonte
                )
                Row {
                    Text(
                        PricingCalculator.formatCurrency(subtotal),
                        style = MaterialTheme.typography.titleSmall,
                        color = RobleOscuro,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null,
                            modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (expanded) {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = item.description,
                        onValueChange = { onUpdate(item.copy(description = it)) },
                        label = { Text("Descripción del trabajo / material") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        minLines = 2
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = if (item.quantity == 0.0) "" else item.quantity.toString(),
                            onValueChange = { onUpdate(item.copy(quantity = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Cantidad") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        var unitExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = item.unit,
                                onValueChange = { onUpdate(item.copy(unit = it)) },
                                label = { Text("Unidad") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                                listOf("unidad", "m²", "ml", "hora", "global", "juego").forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = { onUpdate(item.copy(unit = unit)); unitExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                            onValueChange = { onUpdate(item.copy(unitPrice = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Precio unitario") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = if (item.discountPercent == 0.0) "" else item.discountPercent.toString(),
                            onValueChange = { onUpdate(item.copy(discountPercent = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Descuento (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    Text("Fotos del trabajo:", style = MaterialTheme.typography.bodySmall, color = CafeMonte)
                    PhotoPickerRow(
                        photos = item.photoPaths,
                        onPhotoAdded = onAddPhoto,
                        onPhotoRemoved = onRemovePhoto
                    )
                }
            } else {
                Text(
                    item.description.ifBlank { "Sin descripción" },
                    style = MaterialTheme.typography.bodySmall,
                    color = CafeMonte,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = RobleOscuro)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = RobleOscuro)
    }
}

@Composable
private fun EditorCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CremaPapel),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = RobleOscuro,
                modifier = Modifier.padding(bottom = 4.dp))
            content()
        }
    }
}
