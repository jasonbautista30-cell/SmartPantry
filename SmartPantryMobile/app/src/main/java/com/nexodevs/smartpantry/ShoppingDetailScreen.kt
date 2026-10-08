package com.nexodevs.smartpantry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexodevs.smartpantry.data.local.SessionManager
import com.nexodevs.smartpantry.data.remote.CreateDetalleListaRequest
import com.nexodevs.smartpantry.data.remote.DetalleListaResponse
import com.nexodevs.smartpantry.data.remote.ListaCompraResponse
import com.nexodevs.smartpantry.data.remote.ProductoResponse
import com.nexodevs.smartpantry.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun ShoppingDetailScreen(
    sessionManager: SessionManager,
    lista: ListaCompraResponse,
    onBack: () -> Unit
) {
    val fondo = Color(0xFF0F172A)
    val tarjeta = Color(0xFF1E293B)
    val verde = Color(0xFF22C55E)
    val azul = Color(0xFF2563EB)
    val textoSecundario = Color(0xFF94A3B8)
    val rojo = Color(0xFFF87171)

    var listaActual by remember { mutableStateOf(lista) }
    var detalles by remember { mutableStateOf<List<DetalleListaResponse>>(emptyList()) }
    var productosDisponibles by remember { mutableStateOf<List<ProductoResponse>>(emptyList()) }

    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var productoSeleccionado by remember { mutableStateOf<ProductoResponse?>(null) }
    var cantidadTexto by remember { mutableStateOf("") }
    var menuProductosAbierto by remember { mutableStateOf(false) }

    var agregandoProducto by remember { mutableStateOf(false) }
    var completandoLista by remember { mutableStateOf(false) }
    var togglingDetalleId by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()

    // Cargar detalles de la lista y productos disponibles del usuario
    fun cargarDatos() {
        cargando = true
        error = ""

        scope.launch {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) {
                    error = "No se encontró una sesión activa."
                    cargando = false
                    return@launch
                }

                val authHeader = "Bearer $token"

                // Cargar productos disponibles para el dropdown
                try {
                    val prods = RetrofitClient.apiService.getProductos(authHeader)
                    productosDisponibles = prods
                    if (prods.isNotEmpty() && productoSeleccionado == null) {
                        productoSeleccionado = prods.first()
                    }
                } catch (_: Exception) {
                    // Si falla productos, no detenemos la carga de detalles
                }

                // Cargar detalles de la lista
                val todosDetalles = RetrofitClient.apiService.getDetallesListaCompras(
                    authorization = authHeader,
                    idLista = listaActual.idLista
                )

                // Filtrar por la lista actual por seguridad
                detalles = todosDetalles.filter { it.idLista == listaActual.idLista }

            } catch (e: HttpException) {
                error = when (e.code()) {
                    401 -> "La sesión expiró. Inicia sesión nuevamente."
                    else -> "No se pudieron cargar los detalles (${e.code()})."
                }
            } catch (e: Exception) {
                error = e.message ?: "Error al conectar con el servidor."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(lista.idLista) {
        cargarDatos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Botón volver
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = tarjeta)
        ) {
            Text(
                text = "← Volver",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Encabezado de la Lista
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listaActual.nombreLista,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Creada: ${listaActual.fechaCreacion.take(10)}",
                    color = textoSecundario,
                    fontSize = 13.sp
                )
            }

            val esCompletada = listaActual.estado.equals("Completada", ignoreCase = true)
            Text(
                text = if (esCompletada) "✓ Completada" else listaActual.estado,
                color = if (esCompletada) verde else Color(0xFFFBBF24),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Acciones principales (Completar lista)
        val esCompletada = listaActual.estado.equals("Completada", ignoreCase = true)

        if (!esCompletada) {
            Button(
                onClick = {
                    completandoLista = true
                    error = ""
                    mensaje = ""

                    scope.launch {
                        try {
                            val token = sessionManager.getToken()
                            if (token.isNullOrBlank()) {
                                error = "No hay sesión activa."
                                return@launch
                            }

                            val resp = RetrofitClient.apiService.completarListaCompra(
                                "Bearer $token",
                                listaActual.idLista
                            )

                            if (resp.isSuccessful) {
                                listaActual = listaActual.copy(estado = "Completada")
                                mensaje = "¡Lista marcada como completada!"
                            } else {
                                error = "Error al completar la lista (${resp.code()})."
                            }
                        } catch (e: HttpException) {
                            error = when (e.code()) {
                                401 -> "Sesión expirada. Inicia sesión de nuevo."
                                else -> "Error (${e.code()}) al completar la lista."
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "No se pudo completar la lista."
                        } finally {
                            completandoLista = false
                        }
                    }
                },
                enabled = !completandoLista,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = verde)
            ) {
                if (completandoLista) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(
                        text = "✓ Marcar lista como completada",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón para desplegar formulario de agregar producto
            Button(
                onClick = {
                    mostrarFormulario = !mostrarFormulario
                    error = ""
                    mensaje = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = azul)
            ) {
                Text(
                    text = if (mostrarFormulario) "Cancelar" else "+ Agregar producto a la lista",
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Tarjeta informativa si ya está completada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tarjeta, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "🎉 Esta lista ha sido completada.",
                    color = verde,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Formulario Agregar Producto
        if (mostrarFormulario && !esCompletada) {
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tarjeta, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "Agregar producto",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (productosDisponibles.isEmpty()) {
                    Text(
                        text = "No tienes productos registrados en tu catálogo.",
                        color = Color(0xFFFBBF24)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Registra un producto en la sección 'Registrar Producto' del menú principal.",
                        color = textoSecundario,
                        fontSize = 13.sp
                    )
                } else {
                    // Selector de producto
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { menuProductosAbierto = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF334155)
                            )
                        ) {
                            Text(
                                text = "Producto: ${productoSeleccionado?.nombre ?: "Seleccionar"}",
                                color = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = menuProductosAbierto,
                            onDismissRequest = { menuProductosAbierto = false }
                        ) {
                            productosDisponibles.forEach { prod ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${prod.nombre} (${prod.unidadMedida ?: "u"})")
                                    },
                                    onClick = {
                                        productoSeleccionado = prod
                                        menuProductosAbierto = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cantidad
                    OutlinedTextField(
                        value = cantidadTexto,
                        onValueChange = {
                            cantidadTexto = it.filter { c -> c.isDigit() }
                            error = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Cantidad a comprar") },
                        placeholder = { Text("Ej: 2") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val prod = productoSeleccionado
                            val cant = cantidadTexto.toIntOrNull()

                            when {
                                prod == null -> {
                                    error = "Selecciona un producto."
                                }
                                cant == null || cant <= 0 -> {
                                    error = "Escribe una cantidad válida mayor a 0."
                                }
                                else -> {
                                    agregandoProducto = true
                                    error = ""
                                    mensaje = ""

                                    scope.launch {
                                        try {
                                            val token = sessionManager.getToken()
                                            if (token.isNullOrBlank()) {
                                                error = "No existe una sesión activa."
                                                return@launch
                                            }

                                            val req = CreateDetalleListaRequest(
                                                idLista = listaActual.idLista,
                                                idProducto = prod.idProducto,
                                                cantidadAComprar = cant,
                                                comprado = false
                                            )

                                            val nuevoDetalle = RetrofitClient.apiService.crearDetalleListaCompra(
                                                "Bearer $token",
                                                req
                                            )

                                            // Asegurar objeto producto no nulo para visualización
                                            val detalleFinal = if (nuevoDetalle.producto == null) {
                                                nuevoDetalle.copy(producto = prod)
                                            } else {
                                                nuevoDetalle
                                            }

                                            detalles = detalles + detalleFinal
                                            cantidadTexto = ""
                                            mostrarFormulario = false
                                            mensaje = "Producto agregado correctamente."

                                        } catch (e: HttpException) {
                                            val cuerpo = e.response()?.errorBody()?.string()
                                            error = if (cuerpo.isNullOrBlank()) {
                                                "Error al agregar producto (${e.code()})."
                                            } else {
                                                "Error (${e.code()}): $cuerpo"
                                            }
                                        } catch (e: Exception) {
                                            error = e.message ?: "No se pudo agregar el producto."
                                        } finally {
                                            agregandoProducto = false
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !agregandoProducto && productoSeleccionado != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = verde)
                    ) {
                        if (agregandoProducto) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Text(
                                text = "Guardar en la lista",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (mensaje.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = mensaje,
                color = verde,
                fontWeight = FontWeight.Bold
            )
        }

        if (error.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                color = rojo
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de detalles
        when {
            cargando -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(color = verde)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Cargando productos...", color = textoSecundario)
                }
            }

            detalles.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tarjeta, RoundedCornerShape(18.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📝", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sin productos en la lista",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (esCompletada) "Esta lista fue completada sin productos." else "Agrega productos usando el botón superior.",
                        color = textoSecundario,
                        fontSize = 13.sp
                    )
                }
            }

            else -> {
                Text(
                    text = "Productos en la lista (${detalles.size})",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(detalles, key = { it.idDetalle }) { item ->
                        val nombreProducto = item.producto?.nombre
                            ?: productosDisponibles.find { it.idProducto == item.idProducto }?.nombre
                            ?: "Producto #${item.idProducto}"

                        val unidad = item.producto?.unidadMedida
                            ?: productosDisponibles.find { it.idProducto == item.idProducto }?.unidadMedida
                            ?: ""

                        DetalleItemCard(
                            item = item,
                            nombreProducto = nombreProducto,
                            unidadMedida = unidad,
                            deshabilitado = esCompletada || togglingDetalleId == item.idDetalle,
                            onToggleComprado = {
                                togglingDetalleId = item.idDetalle
                                error = ""

                                scope.launch {
                                    val token = sessionManager.getToken()
                                    if (token.isNullOrBlank()) {
                                        error = "Sesión expirada."
                                        togglingDetalleId = null
                                        return@launch
                                    }

                                    // Cambio optimista local
                                    val nuevoEstado = !item.comprado
                                    detalles = detalles.map { d ->
                                        if (d.idDetalle == item.idDetalle) d.copy(comprado = nuevoEstado) else d
                                    }

                                    try {
                                        val resp = RetrofitClient.apiService.toggleCompradoDetalle(
                                            "Bearer $token",
                                            item.idDetalle
                                        )

                                        if (!resp.isSuccessful) {
                                            // Revertir
                                            detalles = detalles.map { d ->
                                                if (d.idDetalle == item.idDetalle) d.copy(comprado = item.comprado) else d
                                            }
                                            error = "No se pudo actualizar el estado (${resp.code()})."
                                        }
                                    } catch (e: Exception) {
                                        // Revertir
                                        detalles = detalles.map { d ->
                                            if (d.idDetalle == item.idDetalle) d.copy(comprado = item.comprado) else d
                                        }
                                        error = e.message ?: "Error al actualizar estado."
                                    } finally {
                                        togglingDetalleId = null
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleItemCard(
    item: DetalleListaResponse,
    nombreProducto: String,
    unidadMedida: String,
    deshabilitado: Boolean,
    onToggleComprado: () -> Unit
) {
    val tarjeta = Color(0xFF1E293B)
    val verde = Color(0xFF22C55E)
    val textoSecundario = Color(0xFF94A3B8)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tarjeta, RoundedCornerShape(14.dp))
            .clickable(enabled = !deshabilitado) {
                onToggleComprado()
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = item.comprado,
                onCheckedChange = { if (!deshabilitado) onToggleComprado() },
                enabled = !deshabilitado,
                colors = CheckboxDefaults.colors(
                    checkedColor = verde,
                    uncheckedColor = Color(0xFF64748B),
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = nombreProducto,
                    color = if (item.comprado) textoSecundario else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Cantidad: ${item.cantidadAComprar} $unidadMedida",
                    color = textoSecundario,
                    fontSize = 13.sp
                )
            }
        }

        Text(
            text = if (item.comprado) "Comprado" else "Pendiente",
            color = if (item.comprado) verde else Color(0xFFFBBF24),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
