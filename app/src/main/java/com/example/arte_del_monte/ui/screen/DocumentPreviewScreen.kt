package com.example.arte_del_monte.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arte_del_monte.ui.theme.*
import com.example.arte_del_monte.ui.viewmodel.ExportState
import com.example.arte_del_monte.ui.viewmodel.ExportViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DocumentPreviewScreen(
    documentId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vm: ExportViewModel = viewModel(factory = ExportViewModel.Factory(context))
    val exportState by vm.exportState.collectAsStateWithLifecycle()

    LaunchedEffect(documentId) {
        vm.prepareExport(documentId)
    }

    Scaffold(
        containerColor = NegroMate,
        topBar = {
            TopAppBar(
                title = { Text("Vista previa", color = CremaPapel) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Atrás", tint = CremaPapel)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NegroMate)
            )
        },
        bottomBar = {
            if (exportState is ExportState.Ready) {
                val ready = exportState as ExportState.Ready
                Surface(
                    color = CremaPapel,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { ready.pdfUri?.let { vm.sharePdf(it) } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RobleOscuro)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("PDF")
                        }
                        OutlinedButton(
                            onClick = { vm.shareImages(ready.imageUris) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RobleOscuro)
                        ) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Imagen")
                        }
                        Button(
                            onClick = { ready.pdfUri?.let { vm.sharePdf(it) } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CafeMonte, contentColor = CremaPapel)
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Compartir")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = exportState) {
                is ExportState.Idle, is ExportState.Rendering -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CremaPapel)
                        Spacer(Modifier.height(16.dp))
                        Text("Generando vista previa...", color = CremaPapel)
                    }
                }
                is ExportState.Ready -> {
                    if (state.pages.isEmpty()) {
                        Text("Sin páginas para mostrar", color = CremaPapel)
                    } else {
                        val pagerState = rememberPagerState { state.pages.size }
                        Column(modifier = Modifier.fillMaxSize()) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) { pageIndex ->
                                PageView(bitmap = state.pages[pageIndex])
                            }
                            if (state.pages.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "Página ${pagerState.currentPage + 1} de ${state.pages.size}",
                                        color = CremaPapel,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                is ExportState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(state.message, color = CremaPapel)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { vm.prepareExport(documentId) }) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageView(bitmap: Bitmap) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Página del documento",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
