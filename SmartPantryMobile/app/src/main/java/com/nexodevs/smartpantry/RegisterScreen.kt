package com.nexodevs.smartpantry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexodevs.smartpantry.data.local.SessionManager
import com.nexodevs.smartpantry.data.remote.RegisterRequest
import com.nexodevs.smartpantry.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun RegisterScreen(
    sessionManager: SessionManager,
    onRegisterSuccessWithSession: () -> Unit,
    onRegisterSuccessNeedLogin: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var mostrarPassword by remember { mutableStateOf(false) }
    var mostrarConfirmPassword by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val fondo = Color(0xFF0F172A)
    val tarjeta = Color(0xFF1E293B)
    val verde = Color(0xFF16A34A)
    val textoSecundario = Color(0xFF94A3B8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(
                    color = tarjeta,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🥗",
                fontSize = 50.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Smart Pantry",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "ESFE-ÁGAPE • NEXO DEVS",
                color = textoSecundario,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Crear cuenta",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Únete para gestionar tu despensa inteligente",
                modifier = Modifier.fillMaxWidth(),
                color = textoSecundario,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Campo Nombre Completo
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre completo") },
                placeholder = { Text("Ej: Juan Pérez") },
                singleLine = true,
                colors = camposColores()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Correo
            OutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo electrónico") },
                placeholder = { Text("tucorreo@ejemplo.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = camposColores()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { mostrarPassword = !mostrarPassword }) {
                        Text(
                            text = if (mostrarPassword) "Ocultar" else "Mostrar",
                            color = verde
                        )
                    }
                },
                colors = camposColores()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Confirmar Contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                visualTransformation = if (mostrarConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { mostrarConfirmPassword = !mostrarConfirmPassword }) {
                        Text(
                            text = if (mostrarConfirmPassword) "Ocultar" else "Mostrar",
                            color = verde
                        )
                    }
                },
                colors = camposColores()
            )

            if (error.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = error,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF87171),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Botón Crear Cuenta
            Button(
                onClick = {
                    val correoLimpio = correo.trim()
                    val nombreLimpio = nombre.trim()

                    when {
                        nombreLimpio.isBlank() || correoLimpio.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            error = "Por favor completa todos los campos."
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches() -> {
                            error = "Ingresa un correo electrónico válido."
                        }
                        password.length < 6 -> {
                            error = "La contraseña debe tener al menos 6 caracteres."
                        }
                        password != confirmPassword -> {
                            error = "Las contraseñas no coinciden."
                        }
                        else -> {
                            loading = true
                            error = ""

                            scope.launch {
                                try {
                                    val req = RegisterRequest(
                                        nombre = nombreLimpio,
                                        correo = correoLimpio,
                                        password = password,
                                        rol = "Usuario"
                                    )

                                    val response = RetrofitClient.apiService.register(req)

                                    if (response.isSuccessful) {
                                        val auth = response.body()
                                        if (auth != null && auth.token.isNotBlank()) {
                                            sessionManager.saveSession(
                                                token = auth.token,
                                                idUsuario = auth.idUsuario,
                                                nombre = auth.nombre,
                                                correo = auth.correo,
                                                rol = auth.rol
                                            )
                                            onRegisterSuccessWithSession()
                                        } else {
                                            onRegisterSuccessNeedLogin("Cuenta creada correctamente. Inicia sesión.")
                                        }
                                    } else {
                                        val errorBodyStr = response.errorBody()?.string() ?: ""
                                        error = when {
                                            response.code() == 409 || errorBodyStr.contains("existe", ignoreCase = true) -> {
                                                "El correo electrónico ya está registrado."
                                            }
                                            response.code() == 400 -> {
                                                if (errorBodyStr.isNotBlank()) "Error (${response.code()}): $errorBodyStr"
                                                else "Revisa la información ingresada."
                                            }
                                            else -> "Error del servidor (${response.code()}). Intenta de nuevo."
                                        }
                                    }
                                } catch (e: HttpException) {
                                    error = when (e.code()) {
                                        409 -> "El correo electrónico ya está registrado."
                                        400 -> "Revisa los datos ingresados."
                                        else -> "Error en el registro (${e.code()})."
                                    }
                                } catch (_: IOException) {
                                    error = "No se pudo conectar con Smart Pantry."
                                } catch (e: Exception) {
                                    error = e.message ?: "Ocurrió un error inesperado."
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = verde)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(
                        text = "Crear cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Volver a Iniciar Sesión
            TextButton(
                onClick = onBackToLogin,
                enabled = !loading
            ) {
                Text(
                    text = "← Volver a iniciar sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun camposColores() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF16A34A),
    unfocusedBorderColor = Color(0xFF475569),
    focusedLabelColor = Color(0xFF22C55E),
    unfocusedLabelColor = Color(0xFF94A3B8),
    cursorColor = Color(0xFF22C55E)
)
