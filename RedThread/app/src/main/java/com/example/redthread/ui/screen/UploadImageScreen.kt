package com.example.redthread.ui.screen.catalog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.redthread.ui.viewmodel.CatalogViewModel

@Composable
fun UploadImageScreen(
    productId: Int,
    vm: CatalogViewModel,
    onFinish: () -> Unit
) {
    var imageUrl by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }

    val pickImage =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            fileUri = uri
        }

    Column(
        Modifier.fillMaxSize().padding(24.dp)
    ) {
        Text("Subir imagen", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("URL (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { pickImage.launch("image/*") }
        ) {
            Text("Seleccionar archivo")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    imageUrl.isNotBlank() ->
                        vm.uploadImageFromUrl(productId, imageUrl) { onFinish() }

                    fileUri != null ->
                        vm.uploadImageFile(productId, fileUri!!) { onFinish() }

                    else -> return@Button
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Subir y finalizar")
        }
    }
}
