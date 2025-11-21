package com.example.redthread.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.redthread.data.local.address.AddressEntity
import com.example.redthread.domain.enums.UserRole
import com.example.redthread.navigation.Route
import com.example.redthread.ui.theme.AccentRed
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.ProfileState
import com.example.redthread.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    role: UserRole,
    onLogout: () -> Unit,
    onGoAdmin: () -> Unit,
    onGoDespachador: () -> Unit,
    navController: NavHostController, // 👈 agregado
    vm: ProfileViewModel = viewModel()
)
 {
    val state by vm.state.collectAsStateWithLifecycle()

    var showAddressDialog by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<AddressEntity?>(null) }

    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePass by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Toasts (success / error)
    LaunchedEffect(state.success) {
        state.success?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            vm.clearMessages()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            vm.clearMessages()
        }
    }

    Scaffold(
        containerColor = Black,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()) // ✅ scrolleable
        ) {

            Text("Mi Perfil", color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))

            when {
                state.loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                state.user == null -> {
                    Text("No hay sesión activa.", color = TextSecondary)
                }
                else -> {
                    // Encabezado + botón Editar (abre modal)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            EncabezadoPerfil(state)
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (role) {
                                    UserRole.ADMINISTRADOR ->
                                        BotonRol(texto = "Ir al Panel de Administrador", onClick = onGoAdmin)
                                    UserRole.DESPACHADOR ->
                                        BotonRol(texto = "Ir al Panel de Despachador", onClick = onGoDespachador)
                                    else -> Spacer(Modifier.width(1.dp))
                                }
                                TextButton(onClick = { showEditProfile = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Editar perfil")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ===== Sección direcciones =====
                    SeccionDirecciones(
                        addresses = state.addresses,
                        onAdd = { editingAddress = null; showAddressDialog = true },
                        onEdit = { editingAddress = it; showAddressDialog = true },
                        onDelete = { vm.deleteAddress(it) },
                        onSetDefault = { vm.setDefaultAddress(it.id) }
                    )

                    Spacer(Modifier.height(24.dp))

                    // Botón cerrar sesión (siempre accesible por el scroll de la página)
                    // Botón historial de compras
                    // Botón historial de compras
                    Button(
                        onClick = {
                            navController.navigate(Route.HistorialCompras.path) {
                                launchSingleTop = true
                                popUpTo(Route.Perfil.path) { inclusive = false }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text("Ver historial de compras", color = TextPrimary)
                    }


// Botón cerrar sesión
                    Button(
                        onClick = {
                            try {
                                onLogout()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar sesión", color = TextPrimary)
                    }


                }
            }
        }
    }

    // ===== Modales =====
    if (showAddressDialog) {
        DialogDireccion(
            edit = editingAddress,
            onDismiss = { showAddressDialog = false; vm.clearMessages() },
            onSave = { alias, l1, l2, comuna, ciudad, region, pais, cp, pred ->
                vm.upsertAddress(alias, l1, l2, comuna, ciudad, region, pais, cp, pred, editingAddress?.id)
                showAddressDialog = false
            }
        )
    }

    if (showEditProfile) {
        EditProfileDialog(
            state = state,
            onDismiss = { showEditProfile = false; vm.clearMessages() },
            onSave = { email, phone ->
                vm.updateEmailPhone(email, phone)
                showEditProfile = false
            },
            onChangePass = { showEditProfile = false; showChangePass = true }
        )
    }

    if (showChangePass) {
        ChangePasswordDialog(
            onDismiss = { showChangePass = false; vm.clearMessages() },
            onSubmit = { old, new, confirm ->
                vm.changePassword(old, new, confirm)
                showChangePass = false
            }
        )
    }
}

@Composable
private fun EncabezadoPerfil(state: ProfileState) {
    val u = state.user!!
    Column {
        Text(u.name, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(u.email, color = TextSecondary)
        if (u.phone.isNotBlank()) Text("Tel: ${u.phone}", color = TextSecondary)
    }
}

@Composable
private fun BotonRol(texto: String, onClick: () -> Unit) {
    Button(onClick = onClick) { Text(texto) }
}

/* ===============================
   Modal Editar perfil (email/teléfono)
   =============================== */
@Composable
private fun EditProfileDialog(
    state: ProfileState,
    onDismiss: () -> Unit,
    onSave: (email: String, phone: String) -> Unit,
    onChangePass: () -> Unit
) {
    val u = state.user!!
    var email by remember(u.email) { mutableStateOf(u.email) }
    var phone by remember(u.phone) { mutableStateOf(u.phone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(email.trim(), phone.trim()) }) { Text("Guardar") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onChangePass) {
                    Icon(Icons.Filled.Lock, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Cambiar contraseña")
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        },
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Nombre bloqueado
                OutlinedTextField(
                    value = u.name,
                    onValueChange = {},
                    label = { Text("Nombre") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}

/* ===============================
   Sección de direcciones (sin LazyColumn para evitar nested scroll)
   =============================== */
@Composable
private fun SeccionDirecciones(
    addresses: List<AddressEntity>,
    onAdd: () -> Unit,
    onEdit: (AddressEntity) -> Unit,
    onDelete: (AddressEntity) -> Unit,
    onSetDefault: (AddressEntity) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Direcciones", color = TextPrimary, fontWeight = FontWeight.Bold)
        TextButton(onClick = onAdd) { Text("Añadir") }
    }

    if (addresses.isEmpty()) {
        Text("Aún no tienes direcciones guardadas.", color = TextSecondary)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            addresses.forEach { dir ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (dir.predeterminada) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            } else {
                                Icon(Icons.Outlined.StarOutline, contentDescription = null)
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("${dir.alias} — ${dir.ciudad}")
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(dir.linea1)
                        dir.linea2?.takeIf { it.isNotBlank() }?.let { Text(it) }
                        Text("${dir.comuna}, ${dir.region}")
                        Text(dir.pais + (dir.codigoPostal?.let { ", $it" } ?: ""))

                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { onSetDefault(dir) }) {
                                Text(if (dir.predeterminada) "Predeterminada" else "Establecer como predeterminada")
                            }
                            Row {
                                IconButton(onClick = { onEdit(dir) }) { Icon(Icons.Filled.Edit, contentDescription = "Editar") }
                                IconButton(onClick = { onDelete(dir) }) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar") }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ===============================
   Diálogo crear/editar dirección
   =============================== */
@Composable
private fun DialogDireccion(
    edit: AddressEntity?,
    onDismiss: () -> Unit,
    onSave: (
        alias: String, l1: String, l2: String?,
        comuna: String, ciudad: String, region: String,
        pais: String, cp: String?, pred: Boolean
    ) -> Unit
) {
    var alias by remember { mutableStateOf(edit?.alias ?: "") }
    var l1 by remember { mutableStateOf(edit?.linea1 ?: "") }
    var l2 by remember { mutableStateOf(edit?.linea2 ?: "") }
    var comuna by remember { mutableStateOf(edit?.comuna ?: "") }
    var ciudad by remember { mutableStateOf(edit?.ciudad ?: "") }
    var region by remember { mutableStateOf(edit?.region ?: "") }
    var pais by remember { mutableStateOf(edit?.pais ?: "Chile") }
    var cp by remember { mutableStateOf(edit?.codigoPostal ?: "") }
    var pred by remember { mutableStateOf(edit?.predeterminada ?: (edit == null)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val canSave = alias.isNotBlank() && l1.isNotBlank() &&
                    comuna.isNotBlank() && ciudad.isNotBlank() && region.isNotBlank()
            Button(onClick = {
                onSave(alias, l1, l2.ifBlank { null }, comuna, ciudad, region, pais, cp.ifBlank { null }, pred)
            }, enabled = canSave) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text(if (edit == null) "Nueva dirección" else "Editar dirección") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = alias, onValueChange = { alias = it }, label = { Text("Alias (Casa, Trabajo)") })
                OutlinedTextField(value = l1, onValueChange = { l1 = it }, label = { Text("Dirección (calle y número)") })
                OutlinedTextField(value = l2, onValueChange = { l2 = it }, label = { Text("Complemento (opcional)") })
                OutlinedTextField(value = comuna, onValueChange = { comuna = it }, label = { Text("Comuna") })
                OutlinedTextField(value = ciudad, onValueChange = { ciudad = it }, label = { Text("Ciudad") })
                OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("Región") })
                OutlinedTextField(value = pais, onValueChange = { pais = it }, label = { Text("País") })
                OutlinedTextField(value = cp, onValueChange = { cp = it }, label = { Text("Código postal (opcional)") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pred, onCheckedChange = { pred = it })
                    Text("Predeterminada")
                }
            }
        }
    )
}

/* ===============================
   Diálogo cambiar contraseña
   =============================== */
@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (old: String, new: String, confirm: String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val canSave = oldPass.isNotBlank() && newPass.length >= 6 && confirm.isNotBlank()
            Button(onClick = { onSubmit(oldPass, newPass, confirm) }, enabled = canSave) {
                Text("Actualizar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = oldPass, onValueChange = { oldPass = it }, label = { Text("Contraseña actual") }, singleLine = true)
                OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("Nueva contraseña (min 6)") }, singleLine = true)
                OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Confirmar nueva") }, singleLine = true)
            }
        }
    )
}
