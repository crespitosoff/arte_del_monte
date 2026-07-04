package com.example.arte_del_monte.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.arte_del_monte.data.settings.BrandSettings
import com.example.arte_del_monte.ui.components.ColorPickerDialog
import com.example.arte_del_monte.ui.theme.*
import com.example.arte_del_monte.ui.viewmodel.BrandSettingsViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vm: BrandSettingsViewModel = viewModel(factory = BrandSettingsViewModel.Factory(context))
    val saved by vm.settings.collectAsStateWithLifecycle()

    var businessName by remember(saved.businessName) { mutableStateOf(saved.businessName) }
    var slogan by remember(saved.slogan) { mutableStateOf(saved.slogan) }
    var phone by remember(saved.phone) { mutableStateOf(saved.phone) }
    var location by remember(saved.location) { mutableStateOf(saved.location) }
    var nit by remember(saved.nit) { mutableStateOf(saved.nit) }
    var logoPath by remember(saved.logoPath) { mutableStateOf(saved.logoPath) }
    var colorPrimary by remember(saved.colorPrimary) { mutableStateOf(saved.colorPrimary) }
    var colorSecondary by remember(saved.colorSecondary) { mutableStateOf(saved.colorSecondary) }
    var colorEmphasis by remember(saved.colorEmphasis) { mutableStateOf(saved.colorEmphasis) }
    var colorBackground by remember(saved.colorBackground) { mutableStateOf(saved.colorBackground) }
    var taxEnabled by remember(saved.taxEnabled) { mutableStateOf(saved.taxEnabled) }
    var taxRate by remember(saved.taxRate) { mutableStateOf(saved.taxRate.toString()) }

    var colorPickerTarget by remember { mutableStateOf<String?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val logoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val dest = File(context.filesDir, "logo.png")
            context.contentResolver.openInputStream(it)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            logoPath = dest.absolutePath
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    colorPickerTarget?.let { target ->
        val current = when (target) {
            "primary" -> colorPrimary
            "secondary" -> colorSecondary
            "emphasis" -> colorEmphasis
            else -> colorBackground
        }
        val title = when (target) {
            "primary" -> "Roble Oscuro (principal)"
            "secondary" -> "Café Monte (secundario)"
            "emphasis" -> "Negro Mate (énfasis)"
            else -> "Crema Papel (fondo)"
        }
        ColorPickerDialog(
            initialColor = current,
            onColorSelected = { color ->
                when (target) {
                    "primary" -> colorPrimary = color
                    "secondary" -> colorSecondary = color
                    "emphasis" -> colorEmphasis = color
                    else -> colorBackground = color
                }
            },
            onDismiss = { colorPickerTarget = null },
            title = title
        )
    }

    Scaffold(
        containerColor = CremaPapelLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración de marca") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        vm.save(BrandSettings(
                            businessName = businessName,
                            slogan = slogan,
                            phone = phone,
                            location = location,
                            nit = nit,
                            logoPath = logoPath,
                            colorPrimary = colorPrimary,
                            colorSecondary = colorSecondary,
                            colorEmphasis = colorEmphasis,
                            colorBackground = colorBackground,
                            taxEnabled = taxEnabled,
                            taxRate = taxRate.toDoubleOrNull() ?: 0.0
                        ))
                        snackbarMessage = "Ajustes guardados"
                    }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCard(title = "Logo del negocio") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, CafeMonte, CircleShape)
                            .clickable { logoPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoPath.isNotBlank()) {
                            AsyncImage(
                                model = File(logoPath),
                                contentDescription = "Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.Landscape,
                                contentDescription = "Sin logo",
                                tint = CafeMonte,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Seleccionar logo", style = MaterialTheme.typography.bodyMedium)
                        Text("PNG con fondo transparente recomendado",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            SettingsCard(title = "Información del negocio") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = businessName, onValueChange = { businessName = it },
                        label = { Text("Nombre del negocio") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true)
                    OutlinedTextField(value = slogan, onValueChange = { slogan = it },
                        label = { Text("Eslogan") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it },
                        label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true)
                    OutlinedTextField(value = location, onValueChange = { location = it },
                        label = { Text("Ubicación") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true)
                    OutlinedTextField(value = nit, onValueChange = { nit = it },
                        label = { Text("NIT (opcional)") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true)
                }
            }

            SettingsCard(title = "Colores de marca") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorRow("Roble Oscuro (principal)", colorPrimary) { colorPickerTarget = "primary" }
                    ColorRow("Café Monte (secundario)", colorSecondary) { colorPickerTarget = "secondary" }
                    ColorRow("Negro Mate (énfasis)", colorEmphasis) { colorPickerTarget = "emphasis" }
                    ColorRow("Crema Papel (fondo)", colorBackground) { colorPickerTarget = "background" }
                }
            }

            SettingsCard(title = "Impuesto / IVA") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aplicar impuesto en documentos", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = taxEnabled, onCheckedChange = { taxEnabled = it })
                    }
                    if (taxEnabled) {
                        OutlinedTextField(
                            value = taxRate,
                            onValueChange = { taxRate = it },
                            label = { Text("Porcentaje de impuesto (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            Button(
                onClick = {
                    vm.save(BrandSettings(
                        businessName = businessName, slogan = slogan, phone = phone,
                        location = location, nit = nit, logoPath = logoPath,
                        colorPrimary = colorPrimary, colorSecondary = colorSecondary,
                        colorEmphasis = colorEmphasis, colorBackground = colorBackground,
                        taxEnabled = taxEnabled, taxRate = taxRate.toDoubleOrNull() ?: 0.0
                    ))
                    snackbarMessage = "Ajustes guardados"
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CafeMonte, contentColor = CremaPapel)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar ajustes")
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CremaPapel),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun ColorRow(label: String, color: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "#${Integer.toHexString(color).uppercase().drop(2)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(end = 8.dp)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(color))
                    .border(1.dp, CafeMonte.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            )
        }
    }
}
