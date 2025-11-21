package com.example.redthread.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.redthread.ui.theme.Black

@Composable
fun AppTopBar(
    onLogoClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {},
    onCarritoClick: () -> Unit = {},
    cartCount: Int = 0
) {
    val ctx = LocalContext.current
    val logoId = ctx.resources.getIdentifier("logo_redthread", "drawable", ctx.packageName)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (logoId != 0) {
                Image(
                    painter = painterResource(id = logoId),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(145.dp)
                        .clickable { onLogoClick() }
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(onClick = onPerfilClick) {
                Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = Color.White)
            }

            // Icono de carrito con badge centrado
            Box(modifier = Modifier.wrapContentSize()) {
                IconButton(onClick = onCarritoClick) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Carrito", tint = Color.White)
                }
                if (cartCount > 0) {
                    val label = if (cartCount > 9) "+9" else cartCount.toString()

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)      // anclado a esquina superior derecha del icono
                            .offset(x = 2.dp, y = (-2).dp) // ajuste fino de posición
                            .size(18.dp)
                            .background(Color(0xFFE53935), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 10.sp,
                            lineHeight = 10.sp,           // ayuda a centrar verticalmente
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }
            }
        }

        Divider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.08f))
    }
}
