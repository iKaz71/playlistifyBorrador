package com.kaz.playlistify.ui.screens.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaz.playlistify.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaTopBar(
    onBuscarClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo), // Logo en drawable/mipmap
                    contentDescription = "Logo Playlistify",
                    modifier = Modifier.width(28.dp),
                    tint = Color.Unspecified // Respeta los colores originales del logo
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Playlistify",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onBuscarClick) {
                Icon(Icons.Default.Search, contentDescription = "Buscar en YouTube", tint = Color.White)
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF181818)
        )
    )
}
