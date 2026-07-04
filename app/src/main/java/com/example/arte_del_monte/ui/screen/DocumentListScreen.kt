package com.example.arte_del_monte.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arte_del_monte.data.db.entity.DocumentEntity
import com.example.arte_del_monte.data.model.DocumentType
import com.example.arte_del_monte.ui.theme.*
import com.example.arte_del_monte.ui.viewmodel.DocumentListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    onBack: () -> Unit,
    onOpenDocument: (Long) -> Unit,
    onDuplicateDocument: (Long) -> Unit
) {
    val context = LocalContext.current
    val vm: DocumentListViewModel = viewModel(factory = DocumentListViewModel.Factory(context))
    val documents by vm.documents.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<DocumentType?>(null) }
    var docToDelete by remember { mutableStateOf<DocumentEntity?>(null) }

    // Sync filters to VM
    LaunchedEffect(searchQuery) { vm.setClientQuery(searchQuery) }
    LaunchedEffect(selectedType) { vm.setTypeFilter(selectedType) }

    docToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = { Text("Eliminar ${doc.number}") },
            text = { Text("¿Eliminar este documento permanentemente?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteDocument(doc.id)
                    docToDelete = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        containerColor = CremaPapelLight,
        topBar = {
            TopAppBar(
                title = { Text("Historial de documentos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CremaPapel,
                    titleContentColor = RobleOscuro,
                    navigationIconContentColor = CafeMonte
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por cliente...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("Todos") }
                )
                DocumentType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = if (selectedType == type) null else type },
                        label = { Text(type.label) }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (documents.isEmpty()) {
                    item {
                        Text(
                            "No se encontraron documentos",
                            modifier = Modifier.padding(32.dp),
                            color = CafeMonte
                        )
                    }
                } else {
                    items(documents, key = { it.id }) { doc ->
                        HistoryDocumentCard(
                            doc = doc,
                            onClick = { onOpenDocument(doc.id) },
                            onDuplicate = { onDuplicateDocument(doc.id) },
                            onDelete = { docToDelete = doc }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryDocumentCard(
    doc: DocumentEntity,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val type = try { DocumentType.valueOf(doc.type) } catch (e: Exception) { DocumentType.COTIZACION }
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(doc.date))
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CremaPapel),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        doc.number,
                        style = MaterialTheme.typography.titleMedium,
                        color = RobleOscuro
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = if (type == DocumentType.COTIZACION) CafeMonte else NegroMate,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            type.label,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = CremaPapel
                        )
                    }
                }
                Text(
                    doc.clientName.ifBlank { "Sin cliente" },
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis, maxLines = 1
                )
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = CafeMonte)
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Abrir") },
                        onClick = { showMenu = false; onClick() },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicar") },
                        onClick = { showMenu = false; onDuplicate() },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}
