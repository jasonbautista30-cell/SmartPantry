package com.nexodevs.smartpantry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexodevs.smartpantry.data.local.SessionManager
import com.nexodevs.smartpantry.data.remote.InventarioResponse
import com.nexodevs.smartpantry.data.remote.RetrofitClient
import retrofit2.HttpException

@Composable
fun InventoryScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit
) {

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    var inventario by remember {
        mutableStateOf<List<InventarioResponse>>(emptyList())
    }

    val fondo = Color(0xFF0F172A)
    val tarjeta = Color(0xFF1E293B)
    val verde = Color(0xFF16A34A)
    val textoSecundario = Color(0xFF94A3B8)

    LaunchedEffect(Unit) {

        loading = true
        error = ""

        try {

            val token = sessionManager.getToken()

            if (token.isNullOrBlank()) {

                error = "No se encontró una sesión activa."

            } else {

                inventario =
                    RetrofitClient.apiService.getInventario(
                        "Bearer $token"
                    )
            }

        } catch (e: HttpException) {

            error = when (e.code()) {

                401 ->
                    "La sesión expiró. Inicia sesión nuevamente."

                else ->
                    "Error del servidor (${e.code()})."
            }

        } catch (e: Exception) {

            error =
                e.message ?: "No se pudo cargar el inventario."

        } finally {

            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .padding(24.dp)
    ) {

        Spacer(modifier = Modifier.height(26.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = tarjeta
            )
        ) {
            Text(
                text = "← Volver",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mi Inventario",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Productos guardados en tu despensa",
            color = textoSecundario
        )

        Spacer(modifier = Modifier.height(28.dp))

        when {

            loading -> {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        color = verde
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cargando inventario...",
                        color = textoSecundario
                    )
                }
            }

            error.isNotBlank() -> {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = tarjeta,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(22.dp)
                ) {

                    Text(
                        text = "No se pudo cargar el inventario",
                        color = Color(0xFFF87171),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = error,
                        color = textoSecundario
                    )
                }
            }

            inventario.isEmpty() -> {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = tarjeta,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "📦",
                        fontSize = 50.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tu inventario está vacío",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Registra tu primer producto para comenzar.",
                        color = textoSecundario
                    )
                }
            }

            else -> {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(inventario) { item ->

                        InventoryCard(
                            item = item
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryCard(
    item: InventarioResponse
) {

    val tarjeta = Color(0xFF1E293B)
    val verde = Color(0xFF22C55E)
    val textoSecundario = Color(0xFF94A3B8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = tarjeta,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            text = item.producto?.nombre ?: "Producto",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.producto?.categoria?.nombre
                ?: "Sin categoría",
            color = verde
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Cantidad",
                color = textoSecundario
            )

            Text(
                text =
                    "${item.cantidad} ${item.producto?.unidadMedida ?: ""}",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Ubicación",
                color = textoSecundario
            )

            Text(
                text = item.ubicacion ?: "Sin ubicación",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Vencimiento",
                color = textoSecundario
            )

            Text(
                text = item.fechaVencimiento
                    ?: "Sin fecha",
                color = Color.White
            )
        }
    }
}