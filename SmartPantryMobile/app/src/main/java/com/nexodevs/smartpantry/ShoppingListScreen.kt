package com.nexodevs.smartpantry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexodevs.smartpantry.data.local.SessionManager
import com.nexodevs.smartpantry.data.remote.CreateListaCompraRequest
import com.nexodevs.smartpantry.data.remote.ListaCompraResponse
import com.nexodevs.smartpantry.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ShoppingListScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onSelectList: (ListaCompraResponse) -> Unit = {}
) {

    val fondo = Color(0xFF0F172A)
    val tarjeta = Color(0xFF1E293B)
    val verde = Color(0xFF22C55E)
    val azul = Color(0xFF2563EB)
    val textoSecundario = Color(0xFF94A3B8)

    var listas by remember {
        mutableStateOf<List<ListaCompraResponse>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var nombreNuevaLista by remember {
        mutableStateOf("")
    }

    var creandoLista by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {

        try {

            val token = sessionManager.getToken()

            if (token.isNullOrBlank()) {
                error = "No se encontró una sesión activa."
                return@LaunchedEffect
            }

            listas = RetrofitClient.apiService.getListasCompras(
                "Bearer $token"
            )

        } catch (e: HttpException) {

            error = when (e.code()) {

                401 ->
                    "La sesión expiró. Inicia sesión nuevamente."

                else ->
                    "No se pudieron cargar las listas (${e.code()})."
            }

        } catch (e: Exception) {

            error =
                e.message ?: "No se pudieron cargar las listas de compras."

        } finally {

            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .padding(24.dp)
    ) {

        Spacer(
            modifier = Modifier.height(24.dp)
        )

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

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Lista de Compras",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Organiza los productos que necesitas comprar",
            color = textoSecundario
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {
                mostrarFormulario = !mostrarFormulario
                error = ""
                mensaje = ""
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = azul
            )
        ) {

            Text(
                text = if (mostrarFormulario) {
                    "Cancelar"
                } else {
                    "+ Crear nueva lista"
                },
                fontWeight = FontWeight.Bold
            )
        }

        if (mostrarFormulario) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

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
                    text = "Nueva lista",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = nombreNuevaLista,
                    onValueChange = {
                        nombreNuevaLista = it
                        error = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Nombre de la lista")
                    },
                    placeholder = {
                        Text("Ej: Compras de la semana")
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = verde,
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = verde,
                        unfocusedLabelColor = textoSecundario,
                        cursorColor = verde
                    )
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Button(
                    onClick = {

                        if (nombreNuevaLista.isBlank()) {

                            error = "Escribe un nombre para la lista."

                        } else {

                            creandoLista = true
                            error = ""
                            mensaje = ""

                            scope.launch {

                                try {

                                    val token =
                                        sessionManager.getToken()

                                    if (token.isNullOrBlank()) {

                                        error =
                                            "No existe una sesión activa."

                                        return@launch
                                    }

                                    val nuevaLista =
                                        RetrofitClient.apiService
                                            .crearListaCompra(
                                                "Bearer $token",
                                                CreateListaCompraRequest(
                                                    idLista = 0,
                                                    nombreLista =
                                                        nombreNuevaLista.trim(),
                                                    estado = "Pendiente",
                                                    fechaCreacion =
                                                        fechaActualIso()
                                                )
                                            )

                                    listas = listas + nuevaLista

                                    nombreNuevaLista = ""
                                    mostrarFormulario = false

                                    mensaje =
                                        "Lista creada correctamente."

                                } catch (e: HttpException) {

                                    val cuerpo =
                                        e.response()
                                            ?.errorBody()
                                            ?.string()

                                    error =
                                        if (cuerpo.isNullOrBlank()) {

                                            "Error al crear la lista (${e.code()})."

                                        } else {

                                            "Error (${e.code()}): $cuerpo"
                                        }

                                } catch (e: Exception) {

                                    error =
                                        e.message
                                            ?: "No se pudo crear la lista."

                                } finally {

                                    creandoLista = false
                                }
                            }
                        }
                    },
                    enabled = !creandoLista,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = verde
                    )
                ) {

                    if (creandoLista) {

                        CircularProgressIndicator(
                            color = Color.White
                        )

                    } else {

                        Text(
                            text = "Guardar lista",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (mensaje.isNotBlank()) {

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = mensaje,
                color = verde,
                fontWeight = FontWeight.Bold
            )
        }

        if (error.isNotBlank()) {

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = error,
                color = Color(0xFFF87171)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        when {

            cargando -> {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        color = verde
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "Cargando listas...",
                        color = textoSecundario
                    )
                }
            }

            listas.isEmpty() -> {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = tarjeta,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "🛒",
                        fontSize = 38.sp
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "No tienes listas de compras",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Crea tu primera lista para comenzar.",
                        color = textoSecundario
                    )
                }
            }

            else -> {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(listas) { lista ->

                        ShoppingListCard(
                            lista = lista,
                            onClick = { onSelectList(lista) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingListCard(
    lista: ListaCompraResponse,
    onClick: () -> Unit = {}
) {

    val tarjeta = Color(0xFF1E293B)
    val verde = Color(0xFF22C55E)
    val azul = Color(0xFF60A5FA)
    val textoSecundario = Color(0xFF94A3B8)
    val esCompletada = lista.estado.equals("Completada", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = tarjeta,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(18.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = lista.nombreLista,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (esCompletada) "✓ Completada" else lista.estado,
                color = if (esCompletada) verde else Color(0xFFFBBF24),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Fecha de creación",
                    color = textoSecundario,
                    fontSize = 12.sp
                )

                Text(
                    text = lista.fechaCreacion.take(10),
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "Ver detalles →",
                color = azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun fechaActualIso(): String {

    val formato =
        SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        )

    formato.timeZone =
        TimeZone.getTimeZone("UTC")

    return formato.format(Date())
}