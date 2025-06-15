package com.kaz.playlistify.ui.screens.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaTopBar(
    onBuscarClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Playlistify",
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White
            )
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
