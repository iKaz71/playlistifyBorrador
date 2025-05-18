package com.kaz.playlistify.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaz.playlistify.R
import com.kaz.playlistify.api.RetrofitInstance
import com.kaz.playlistify.model.VerifyRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WelcomeScreen(onJoinClicked: (String) -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var code by remember { mutableStateOf(listOf("", "", "", "")) }
    val focusRequesters = List(4) { remember { FocusRequester() } }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isValidCode = code.all { it.length == 1 }

    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1C1C1E), Color(0xFF0A0A0A), Color(0xFF8E2DE2)),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    fun hideKeyboard() {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    fun verifyCode() {
        hideKeyboard()
        val fullCode = code.joinToString("")
        scope.launch {
            try {
                val response = RetrofitInstance.sessionApi.verifyCode(VerifyRequest(fullCode))
                if (response.isSuccessful && response.body()?.sessionId != null) {
                    val sessionId = response.body()!!.sessionId
                    Log.d("WelcomeScreen", "✅ Código $fullCode verificado → SessionId: $sessionId")
                    onJoinClicked(sessionId)
                } else {
                    errorMessage = "Código inválido o sala no disponible."
                }
            } catch (e: Exception) {
                Log.e("WelcomeScreen", "❌ Error verificando código", e)
                errorMessage = "Error de conexión. Intenta nuevamente."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_playlistify),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Playlistify",
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Bienvenido/a a Playlistify", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Disfruta tu música en equipo.", fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(28.dp))
            Text("Ingresa el código de tu sala activa:", fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                for (i in 0..3) {
                    OutlinedTextField(
                        value = code[i],
                        onValueChange = { input ->
                            if (input.length <= 1 && input.all { it.isDigit() }) {
                                code = code.toMutableList().also { it[i] = input }
                                if (input.isNotEmpty()) {
                                    if (i < 3) focusRequesters[i + 1].requestFocus()
                                    else hideKeyboard()
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .width(60.dp)
                            .height(70.dp)
                            .padding(4.dp)
                            .focusRequester(focusRequesters[i]),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = if (i == 3) ImeAction.Done else ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            cursorColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { verifyCode() },
                enabled = isValidCode,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3EA6FF)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unirse a la sala", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Text(
            text = "© 2025 Playlistify",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}
