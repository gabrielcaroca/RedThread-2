package com.example.redthread.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.redthread.domain.enums.UserRole
import com.example.redthread.data.remote.dto.AddressDto
import com.example.redthread.ui.theme.*
import com.example.redthread.ui.viewmodel.AuthHeaderState
import com.example.redthread.ui.viewmodel.ProfileViewModel
import com.example.redthread.ui.viewmodel.ProfileVmFactory
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    role: UserRole,
    onLogout: () -> Unit,
    onGoAdmin: () -> Unit,
    onGoDespachador: () -> Unit,
    navController: NavHostController,
    header: AuthHeaderState
) {
    // Inicialización del VM con Contexto
    val context = LocalContext.current
    val vm: ProfileViewModel = viewModel(
        factory = ProfileVmFactory(context)
    )

    val state by vm.state.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AddressDto?>(null) }

    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cargar direcciones al abrir
    LaunchedEffect(Unit) { vm.loadAddresses() }

    val dynamicRole = remember(header.role) {
        when (header.role) {
            "ADMIN", "ADMINISTRADOR" -> UserRole.ADMINISTRADOR
            "REPARTIDOR", "DESPACHADOR" -> UserRole.DESPACHADOR
            else -> UserRole.USUARIO
        }
    }

    // Notificaciones
    LaunchedEffect(state.success) {
        state.success?.let {
            scope.launch { snackbar.showSnackbar(it) }
            vm.clearMessages()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            scope.launch { snackbar.showSnackbar(it) }
            vm.clearMessages()
        }
    }

    Scaffold(
        containerColor = Black,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .background(Black)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text("Mi Perfil", color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))

            if (!header.isLoggedIn) {
                Text("No hay sesión activa.", color = TextSecondary)
                return@Column
            }

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(12.dp)) {
                    EncabezadoPerfil(header)

                    Spacer(Modifier.height(10.dp))

                    // ✅ Editar perfil
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showEditProfile = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Editar perfil") }

                        OutlinedButton(
                            onClick = { showChangePassword = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Cambiar contraseña") }
                    }

                    Spacer(Modifier.height(10.dp))

                    when (dynamicRole) {
                        UserRole.ADMINISTRADOR -> BotonRol("Ir al Panel de Administrador", onGoAdmin)
                        UserRole.DESPACHADOR -> BotonRol("Ir al Panel de Despachador", onGoDespachador)
                        else -> {}
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ==========================
            // DIRECCIONES (sin default)
            // ==========================
            SeccionDirecciones(
                addresses = state.addresses,
                onAdd = {
                    editing = null
                    showDialog = true
                },
                onEdit = { dir ->
                    editing = dir
                    showDialog = true
                },
                onDelete = { dir ->
                    vm.deleteAddress(dir.id.toLong())
                }
            )

            Spacer(Modifier.height(24.dp))

            // ✅ (Quitado) Historial de compras

            // Logout
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("Cerrar sesión", color = TextPrimary)
            }
        }
    }

    if (showDialog) {
        DialogDireccion(
            edit = editing,
            onDismiss = { showDialog = false },
            onSave = { line1, city, stateValue, zip, country ->
                if (editing == null) {
                    vm.createAddress(
                        line1 = line1,
                        city = city,
                        state = stateValue,
                        zip = zip,
                        country = country,
                        isDefault = false
                    )
                } else {
                    vm.updateAddress(
                        id = editing!!.id.toLong(),
                        line1 = line1,
                        city = city,
                        state = stateValue,
                        zip = zip,
                        country = country,
                        default = false
                    )
                }
                showDialog = false
            }
        )
    }

    if (showEditProfile) {
        DialogEditarPerfil(
            nameInit = header.displayName ?: "",
            emailInit = header.email ?: "",
            onDismiss = { showEditProfile = false },
            onSave = { fullName, email ->
                vm.updateProfile(fullName = fullName, email = email)
                showEditProfile = false
            }
        )
    }

    if (showChangePassword) {
        DialogCambiarPassword(
            onDismiss = { showChangePassword = false },
            onSave = { current, newPass ->
                vm.changePassword(currentPassword = current, newPassword = newPass)
                showChangePassword = false
            }
        )
    }
}

@Composable
private fun EncabezadoPerfil(header: AuthHeaderState) {
    val name = header.displayName ?: "Usuario"
    val email = header.email.orEmpty()

    Column {
        Text(name, fontWeight = FontWeight.Bold, color = TextPrimary)
        if (email.isNotBlank()) Text(email, color = TextSecondary)
    }
}

@Composable
private fun BotonRol(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) { Text(texto) }
}

// =========================
// LISTA DE DIRECCIONES (sin default)
// =========================
@Composable
private fun SeccionDirecciones(
    addresses: List<AddressDto>,
    onAdd: () -> Unit,
    onEdit: (AddressDto) -> Unit,
    onDelete: (AddressDto) -> Unit
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
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {

                        Text("${dir.city}, ${dir.country}", fontWeight = FontWeight.Medium)

                        Spacer(Modifier.height(6.dp))
                        Text(text = dir.line1)

                        dir.line2
                            ?.takeIf { it.isNotBlank() }
                            ?.let { Text(text = it) }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onEdit(dir) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { onDelete(dir) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogDireccion(
    edit: AddressDto?,
    onDismiss: () -> Unit,
    onSave: (
        line1: String,
        city: String,
        state: String,
        zip: String,
        country: String
    ) -> Unit
) {
    var line1 by remember { mutableStateOf(edit?.line1 ?: "") }
    var city by remember { mutableStateOf(edit?.city ?: "") }
    var state by remember { mutableStateOf(edit?.state ?: "") }
    var zip by remember { mutableStateOf(edit?.zip ?: "") }
    var country by remember { mutableStateOf(edit?.country ?: "Chile") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val canSave = line1.isNotBlank() && city.isNotBlank() &&
                    state.isNotBlank() && country.isNotBlank()

            Button(
                onClick = { onSave(line1, city, state, zip, country) },
                enabled = canSave
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text(if (edit == null) "Nueva dirección" else "Editar dirección") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = line1, onValueChange = { line1 = it }, label = { Text("Dirección") })
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Ciudad") })
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("Región/Estado") })
                OutlinedTextField(value = zip, onValueChange = { zip = it }, label = { Text("Código postal") })
                OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("País") })
            }
        }
    )
}

@Composable
private fun DialogEditarPerfil(
    nameInit: String,
    emailInit: String,
    onDismiss: () -> Unit,
    onSave: (fullName: String, email: String) -> Unit
) {
    var fullName by remember { mutableStateOf(nameInit) }
    var email by remember { mutableStateOf(emailInit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val canSave = fullName.isNotBlank() && email.isNotBlank()
            Button(
                onClick = { onSave(fullName, email) },
                enabled = canSave
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") }
                )
            }
        }
    )
}

@Composable
private fun DialogCambiarPassword(
    onDismiss: () -> Unit,
    onSave: (current: String, newPass: String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val canSave = current.isNotBlank() &&
                    newPass.isNotBlank() &&
                    confirm.isNotBlank() &&
                    (newPass == confirm)

            Button(
                onClick = { onSave(current, newPass) },
                enabled = canSave
            ) { Text("Cambiar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Contraseña actual") }
                )
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("Nueva contraseña") }
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirmar nueva contraseña") }
                )
            }
        }
    )
}
