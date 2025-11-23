package com.example.redthread.ui.screen

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.DespachadorViewModel
import com.example.redthread.ui.viewmodel.Pedido
import com.example.redthread.ui.viewmodel.Ruta
import java.io.File

@Composable
fun DespachadorScreen(vm: DespachadorViewModel = viewModel()) {
    val etapa by vm.etapaSeleccionada
    var pedidoEnDetalle by remember { mutableStateOf<Pedido?>(null) }

    Box(Modifier.fillMaxSize().background(Black)) {

        // =========================
        // 1) Si no hay ruta tomada, mostrar rutas activas
        // =========================
        if (vm.rutaSeleccionada.value == null) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    "Rutas activas",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                if (vm.cargando.value) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFDD3333))
                    }
                } else if (vm.rutasActivas.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay rutas activas", color = TextSecondary)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(vm.rutasActivas) { _, ruta ->
                            RutaCard(
                                ruta = ruta,
                                onTomar = { vm.tomarRuta(ruta.id) }
                            )
                        }
                    }
                }

                vm.error.value?.let { msg ->
                    Spacer(Modifier.height(12.dp))
                    Text("Error: $msg", color = Color(0xFFFF7777), fontSize = 13.sp)
                }
            }

            return@Box
        }

        // =========================
        // 2) Panel con ruta ya asignada
        // =========================
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            val rutaActual = vm.rutaSeleccionada.value?.nombre ?: "Sin ruta"
            Text(
                "Panel de Despacho – $rutaActual",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                vm.etapas.forEach { etapaItem ->
                    val selected = etapaItem == etapa
                    Button(
                        onClick = { vm.cambiarEtapa(etapaItem) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Color(0xFFDD3333) else Color(0xFF2A2A2A)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(etapaItem, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (etapa) {
                "Recoger" -> ListaPedidos(
                    pedidos = vm.pendientes,
                    tipo = "recoger",
                    onRecoger = { pedidoId -> vm.recogerPedido(pedidoId) }
                )

                "Entregar" -> ListaPedidos(
                    pedidos = vm.porEntregar,
                    tipo = "entregar",
                    onMostrarDetalle = { pedidoEnDetalle = it }
                )

                "Retorno" -> ListaPedidos(
                    pedidos = vm.retornos,
                    tipo = "retorno",
                    onMostrarDetalle = { pedidoEnDetalle = it }
                )
            }

            vm.error.value?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text("Error: $msg", color = Color(0xFFFF7777), fontSize = 13.sp)
            }
        }

        // =========================
        // 3) Detalle expandido
        // =========================
        pedidoEnDetalle?.let { pedido ->
            DetallePedidoExpandido(
                pedido = pedido,
                esRetorno = etapa == "Retorno",
                onCerrar = { pedidoEnDetalle = null },
                onConfirmar = { file ->
                    vm.confirmarEntrega(pedido.id, file)
                    pedidoEnDetalle = null
                },
                onDevolver = { motivo, file ->
                    vm.marcarDevuelto(pedido.id, motivo, file)
                    pedidoEnDetalle = null
                }
            )
        }
    }
}

@Composable
fun RutaCard(ruta: Ruta, onTomar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(ruta.nombre, color = TextPrimary, fontWeight = FontWeight.Bold)
                if (ruta.descripcion.isNotBlank()) {
                    Text(ruta.descripcion, color = TextSecondary, fontSize = 13.sp)
                }
                Text("Pedidos: ${ruta.totalPedidos}", color = TextSecondary, fontSize = 12.sp)
            }

            Button(
                onClick = onTomar,
                colors = ButtonDefaults.buttonColors(Color(0xFFDD3333))
            ) {
                Text("Tomar ruta", color = Color.White)
            }
        }
    }
}

@Composable
fun ListaPedidos(
    pedidos: List<Pedido>,
    tipo: String,
    onRecoger: ((Int) -> Unit)? = null,
    onMostrarDetalle: ((Pedido) -> Unit)? = null
) {
    if (pedidos.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin pedidos", color = TextSecondary)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(pedidos) { _, pedido ->
                PedidoCard(
                    pedido = pedido,
                    tipo = tipo,
                    onRecoger = { onRecoger?.invoke(pedido.id) },
                    onMostrarDetalle = { onMostrarDetalle?.invoke(pedido) }
                )
            }
        }
    }
}

@Composable
fun PedidoCard(
    pedido: Pedido,
    tipo: String,
    onRecoger: () -> Unit = {},
    onMostrarDetalle: () -> Unit = {}
) {
    val context = LocalContext.current
    val imgId = context.resources.getIdentifier(pedido.imagen, "drawable", context.packageName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (imgId != 0) {
                Image(
                    painter = painterResource(imgId),
                    contentDescription = pedido.nombre,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(pedido.nombre, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Pedido N°${pedido.id}", color = TextSecondary, fontSize = 13.sp)
            }

            if (tipo == "recoger") {
                Button(
                    onClick = onRecoger,
                    colors = ButtonDefaults.buttonColors(Color(0xFFDD3333))
                ) { Text("Recoger", color = Color.White) }
            }

            if (tipo == "entregar" || tipo == "retorno") {
                Button(
                    onClick = onMostrarDetalle,
                    colors = ButtonDefaults.buttonColors(Color(0xFFDD3333))
                ) { Text("Más info", color = Color.White) }
            }
        }
    }
}

@Composable
fun DetallePedidoExpandido(
    pedido: Pedido,
    esRetorno: Boolean,
    onCerrar: () -> Unit,
    onConfirmar: (File) -> Unit = {},
    onDevolver: (String, File) -> Unit = { _, _ ->}
) {
    val context = LocalContext.current

    var evidenciaFile by remember { mutableStateOf<File?>(null) }
    var fotoTomada by remember { mutableStateOf(false) }

    var mostrarMotivoDialog by remember { mutableStateOf(false) }
    var motivo by remember { mutableStateOf("") }

    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    fun crearArchivoEvidencia(): Pair<File, Uri> {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("ev_${pedido.id}_", ".jpg", dir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return file to uri
    }

    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) fotoTomada = true
    }

    if (mostrarMotivoDialog) {
        AlertDialog(
            onDismissRequest = { mostrarMotivoDialog = false },
            title = { Text("Motivo de devolución") },
            text = {
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Escribe la razón...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val file = evidenciaFile
                    if (file != null) onDevolver(motivo, file)
                    mostrarMotivoDialog = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarMotivoDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C))
            .padding(16.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A2A2A))
                .padding(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCerrar) {
                    Text("✕", color = Color.White, fontSize = 22.sp)
                }
            }

            val imgId = context.resources.getIdentifier(pedido.imagen, "drawable", context.packageName)
            if (imgId != 0) {
                Image(
                    painter = painterResource(imgId),
                    contentDescription = pedido.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(pedido.nombre, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("Pedido N°${pedido.id}", color = TextSecondary, fontSize = 13.sp)

            Divider(Modifier.padding(vertical = 12.dp), color = Color(0xFF444444))

            if (pedido.direccion.isNotBlank())
                Text("Dirección: ${pedido.direccion}", color = TextSecondary)

            if (pedido.mensaje.isNotBlank())
                Text("Mensaje: “${pedido.mensaje}”", color = TextSecondary)

            Divider(Modifier.padding(vertical = 12.dp), color = Color(0xFF444444))

            if (esRetorno) {
                Text("Motivo de devolución:", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(pedido.motivoDevolucion.ifBlank { "Sin motivo registrado" }, color = TextSecondary)
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            permisoCamara.launch(Manifest.permission.CAMERA)
                            val (file, uri) = crearArchivoEvidencia()
                            evidenciaFile = file
                            camaraLauncher.launch(uri)
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFFDD3333)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Foto", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val file = evidenciaFile
                            if (file != null) onConfirmar(file)
                        },
                        enabled = fotoTomada,
                        colors = ButtonDefaults.buttonColors(
                            if (fotoTomada) Color(0xFF4CAF50) else Color(0xFF424242)
                        ),
                        modifier = Modifier.weight(1f)
                    ) { Text("Confirmar", color = Color.White) }

                    Button(
                        onClick = { mostrarMotivoDialog = true },
                        enabled = fotoTomada,
                        colors = ButtonDefaults.buttonColors(
                            if (fotoTomada) Color(0xFFF44336) else Color(0xFF424242)
                        ),
                        modifier = Modifier.weight(1f)
                    ) { Text("Devolver", color = Color.White) }
                }

                if (fotoTomada && evidenciaFile != null) {
                    Spacer(Modifier.height(16.dp))
                    val bmp = BitmapFactory.decodeFile(evidenciaFile!!.absolutePath)
                    bmp?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Text("Evidencia guardada", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
