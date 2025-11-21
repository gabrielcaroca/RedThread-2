package com.example.redthread.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

private enum class ForgotStep { IDENTIFY, OTP, RESET, DONE }

@Composable
fun ForgotPasswordScreenVm(
    vm: AuthViewModel,              // <- lo recibimos desde el NavGraph
    onDoneGoLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(ForgotStep.IDENTIFY) }

    var identifier by remember { mutableStateOf("") }        // email o teléfono
    var otp by remember { mutableStateOf("") }                // cualquier número, simulado
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var info by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Recuperar contraseña", color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        when (step) {
            ForgotStep.IDENTIFY -> {
                Text(
                    "Ingresa tu correo o teléfono. Te enviaremos un código para continuar.",
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it.trim() },
                    label = { Text("Correo o Teléfono") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        error = null
                        if (identifier.isBlank()) {
                            error = "Escribe tu correo o teléfono"
                            return@Button
                        }
                        // Simulado: pasamos al OTP y mostramos el “tipo”
                        val type = if (identifier.contains("@")) "correo" else "SMS"
                        info = "Se te envió un código por $type."
                        step = ForgotStep.OTP
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending
                ) { Text("Enviar código") }

                if (info != null) { Spacer(Modifier.height(8.dp)); Text(info!!, color = TextSecondary) }
                if (error != null) { Spacer(Modifier.height(8.dp)); Text(error!!, color = MaterialTheme.colorScheme.error) }
            }

            ForgotStep.OTP -> {
                Text("Introduce el código recibido", color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it.filter { ch -> ch.isDigit() }.take(6) },
                    label = { Text("Código (6 dígitos)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Simulado: aceptamos cualquier OTP no vacío
                        if (otp.isBlank()) {
                            error = "Ingresa el código"
                            return@Button
                        }
                        error = null
                        step = ForgotStep.RESET
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Verificar") }

                if (error != null) { Spacer(Modifier.height(8.dp)); Text(error!!, color = MaterialTheme.colorScheme.error) }
            }

            ForgotStep.RESET -> {
                Text("Crea tu nueva contraseña", color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        error = null
                        if (newPass.length < 6) {
                            error = "La contraseña debe tener al menos 6 caracteres"; return@Button
                        }
                        if (newPass != confirm) {
                            error = "Las contraseñas no coinciden"; return@Button
                        }
                        isSending = true
                        scope.launch {
                            val ok = vm.resetPasswordByEmailOrPhone(identifier, newPass)
                            isSending = false
                            if (ok) {
                                step = ForgotStep.DONE
                            } else {
                                error = "No encontramos un usuario con esos datos"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending
                ) { Text("Cambiar contraseña") }

                if (error != null) { Spacer(Modifier.height(8.dp)); Text(error!!, color = MaterialTheme.colorScheme.error) }
            }

            ForgotStep.DONE -> {
                Text("¡Listo! Tu contraseña fue actualizada.", color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onDoneGoLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver a iniciar sesión")
                }
            }
        }
    }
}
