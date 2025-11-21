package com.example.redthread.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redthread.ui.theme.*

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun AppDrawer(
    onItemClick: (String) -> Unit,
    onClose: () -> Unit
) {
    val drawerItems = listOf(
        DrawerItem(
            title = "Inicio",
            icon = Icons.Default.Home,
            onClick = { onItemClick("home") }
        ),
        DrawerItem(
            title = "Carrito",
            icon = Icons.Default.ShoppingCart,
            onClick = { onItemClick("carrito") }
        ),
        DrawerItem(
            title = "Perfil",
            icon = Icons.Default.Person,
            onClick = { onItemClick("perfil") }
        ),
        DrawerItem(
            title = "Despachador",
            icon = Icons.Default.DeliveryDining,
            onClick = { onItemClick("despachador") }
        ),
        DrawerItem(
            title = "Despachos",
            icon = Icons.Default.LocalShipping,
            onClick = { onItemClick("despacho") }
        ),
        DrawerItem(
            title = "Despacho Simple",
            icon = Icons.Default.DeliveryDining,
            onClick = { onItemClick("despacho_simple") }
        ),
        DrawerItem(
            title = "Estadísticas",
            icon = Icons.Default.Analytics,
            onClick = { onItemClick("estadisticas_despacho") }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
    ) {
        // Header del drawer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(AccentRed)
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text(
                    text = "RedThread",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gestión de Despachos",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // Lista de opciones
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            items(drawerItems) { item ->
                DrawerItemRow(
                    item = item,
                    onItemClick = {
                        item.onClick()
                        onClose()
                    }
                )
            }
        }
    }
}

@Composable
private fun DrawerItemRow(
    item: DrawerItem,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}