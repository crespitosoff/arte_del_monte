package com.example.arte_del_monte.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
fun HomeScreen(
    onNewDocument: (DocumentType) -> Unit,
    onOpenDocument: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val vm: DocumentListViewModel = viewModel(factory = DocumentListViewModel.Factory(context))
    val documents by vm.documents.collectAsStateWithLifecycle()
    var showNewMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CremaPapelLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Arte de Monte",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Mobiliario macizo de alta gama",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CremaPapel,
                    titleContentColor = RobleOscuro,
                    actionIconContentColor = CafeMonte
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showNewMenu) {
                    DocumentType.entries.forEach { type ->
                        SmallFloatingActionButton(
                            onClick = {
                                showNewMenu = false
                                onNewDocument(type)
                            },
                            containerColor = CremaPapel,
                            contentColor = RobleOscuro,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                type.label,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { showNewMenu = !showNewMenu },
                    containerColor = CafeMonte,
                    contentColor = CremaPapel
                ) {
                    Icon(
                        if (showNewMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Nuevo documento"
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Documentos recientes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            if (documents.isEmpty()) {
                item { EmptyState() }
            } else {
                items(documents.take(10)) { doc ->
                    DocumentCard(
                        doc = doc,
                        onClick = { onOpenDocument(doc.id) }
                    )
                }
                if (documents.size > 10) {
                    item {
                        TextButton(
                            onClick = onOpenHistory,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Ver todos (${documents.size})") }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(doc: DocumentEntity, onClick: () -> Unit) {
    val type = try { DocumentType.valueOf(doc.type) } catch (e: Exception) { DocumentType.COTIZACION }
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(doc.date))

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
            Surface(
                color = if (type == DocumentType.COTIZACION) CafeMonte else NegroMate,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    type.prefix,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(doc.number, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(doc.clientName.ifBlank { "Sin cliente" }, style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis, maxLines = 1)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(dateStr, style = MaterialTheme.typography.bodySmall)
                Text(doc.status, style = MaterialTheme.typography.bodySmall, color = CafeMonte)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = CafeMonte.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Sin documentos aún",
                style = MaterialTheme.typography.bodyLarge,
                color = CafeMonte
            )
            Text(
                "Toca + para crear tu primera cotización",
                style = MaterialTheme.typography.bodyMedium,
                color = CafeMonte.copy(alpha = 0.7f)
            )
        }
    }
}
