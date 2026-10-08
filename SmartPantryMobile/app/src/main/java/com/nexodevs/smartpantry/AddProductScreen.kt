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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexodevs.smartpantry.data.local.SessionManager
import com.nexodevs.smartpantry.data.remote.CategoriaResponse
import com.nexodevs.smartpantry.data.remote.CreateCategoriaRequest
import com.nexodevs.smartpantry.data.remote.CreateInventarioRequest
import com.nexodevs.smartpantry.data.remote.CreateProductoRequest
import com.nexodevs.smartpantry.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun AddProductScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onProductCreated: () -> Unit
) {

    val fondo = Color(0xFF0F172A)
    val tarjeta = Color(0xFF1E293B)
    val verde = Color(0xFF16A34A)
    val textoSecundario = Color(0xFF94A3B8)

    var nombre by remember { mutableStateOf("") }
    var codigoBarras by remember { mutableStateOf("") }
    var unidadMedida by remember { mutableStateOf("Unidades") }
    var cantidad by remember { mutableStateOf("") }
    var cantidadMinima by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }

    var categorias by remember {
        mutableStateOf<List<CategoriaResponse>>(emptyList())
    }

    var categoriaSeleccionada by remember {
        mutableStateOf<CategoriaResponse?>(null)
    }

    var menuCategoriasAbierto by remember {
        mutableStateOf(false)
    }

    var mostrarCrearCategoria by remember {
        mutableStateOf(false)
    }

    var nombreCategoria by remember {
        mutableStateOf("")
    }

    var descripcionCategoria by remember {
        mutableStateOf("")
    }

    var cargandoCategorias by remember {
        mutableStateOf(true)
    }

    var creandoCategoria by remember {
        mutableStateOf(false)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {

        try {

            val token = sessionManager.getToken()

            if (token.isNullOrBlank()) {
                error = "No se encontró una sesión activa."
                return@LaunchedEffect
            }

            categorias = RetrofitClient.apiService.getCategorias(
                "Bearer $token"
            )

            if (categorias.isNotEmpty()) {
                categoriaSeleccionada = categorias.first()
            } else {
                mostrarCrearCategoria = true
            }

        } catch (e: HttpException) {

            error = when (e.code()) {
                401 -> "La sesión expiró. Inicia sesión nuevamente."
                else -> "No se pudieron cargar las categorías (${e.code()})."
            }

        } catch (e: Exception) {

            error = e.message
                ?: "No se pudieron cargar las categorías."

        } finally {
            cargandoCategorias = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .verticalScroll(rememberScrollState())
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
            text = "Registrar Producto",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Añade un producto a tu despensa",
            color = textoSecundario
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = tarjeta,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre del producto") },
                singleLine = true,
                colors = camposColores()
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (cargandoCategorias) {

                CircularProgressIndicator(
                    color = verde
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Cargando categorías...",
                    color = textoSecundario
                )

            } else {

                if (categorias.isNotEmpty()) {

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Button(
                            onClick = {
                                menuCategoriasAbierto = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF334155)
                            )
                        ) {

                            Text(
                                text = "Categoría: ${
                                    categoriaSeleccionada?.nombre
                                        ?: "Seleccionar"
                                }",
                                color = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = menuCategoriasAbierto,
                            onDismissRequest = {
                                menuCategoriasAbierto = false
                            }
                        ) {

                            categorias.forEach { categoria ->

                                DropdownMenuItem(
                                    text = {
                                        Text(categoria.nombre)
                                    },
                                    onClick = {
                                        categoriaSeleccionada = categoria
                                        menuCategoriasAbierto = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            mostrarCrearCategoria =
                                !mostrarCrearCategoria
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155)
                        )
                    ) {

                        Text(
                            text = if (mostrarCrearCategoria) {
                                "Cancelar nueva categoría"
                            } else {
                                "+ Crear nueva categoría"
                            },
                            color = Color.White
                        )
                    }

                } else {

                    Text(
                        text = "Aún no tienes categorías.",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Crea tu primera categoría para continuar.",
                        color = textoSecundario
                    )
                }

                if (mostrarCrearCategoria) {

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nueva categoría",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nombreCategoria,
                        onValueChange = {
                            nombreCategoria = it
                            error = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Nombre de la categoría")
                        },
                        placeholder = {
                            Text("Ej: Lácteos")
                        },
                        singleLine = true,
                        colors = camposColores()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = descripcionCategoria,
                        onValueChange = {
                            descripcionCategoria = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Descripción")
                        },
                        placeholder = {
                            Text("Ej: Leche, queso y yogur")
                        },
                        colors = camposColores()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {

                            if (nombreCategoria.isBlank()) {

                                error =
                                    "Escribe el nombre de la categoría."

                            } else {

                                creandoCategoria = true
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

                                        val nuevaCategoria =
                                            RetrofitClient.apiService
                                                .crearCategoria(
                                                    "Bearer $token",
                                                    CreateCategoriaRequest(
                                                        nombre =
                                                            nombreCategoria.trim(),
                                                        descripcion =
                                                            descripcionCategoria.trim()
                                                    )
                                                )

                                        categorias =
                                            categorias + nuevaCategoria

                                        categoriaSeleccionada =
                                            nuevaCategoria

                                        nombreCategoria = ""
                                        descripcionCategoria = ""

                                        mostrarCrearCategoria = false

                                        mensaje =
                                            "Categoría creada correctamente."

                                    } catch (e: HttpException) {

                                        val cuerpo =
                                            e.response()
                                                ?.errorBody()
                                                ?.string()

                                        error =
                                            if (cuerpo.isNullOrBlank()) {
                                                "Error al crear categoría (${e.code()})."
                                            } else {
                                                "Error (${e.code()}): $cuerpo"
                                            }

                                    } catch (e: Exception) {

                                        error =
                                            e.message
                                                ?: "No se pudo crear la categoría."

                                    } finally {
                                        creandoCategoria = false
                                    }
                                }
                            }
                        },
                        enabled = !creandoCategoria,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        )
                    ) {

                        if (creandoCategoria) {

                            CircularProgressIndicator(
                                color = Color.White
                            )

                        } else {

                            Text(
                                text = "Guardar Categoría",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = codigoBarras,
                onValueChange = {
                    codigoBarras = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Código de barras (opcional)")
                },
                singleLine = true,
                colors = camposColores()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = unidadMedida,
                onValueChange = {
                    unidadMedida = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Unidad de medida")
                },
                placeholder = {
                    Text("Ej: Unidades, kg, litros")
                },
                singleLine = true,
                colors = camposColores()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = cantidad,
                onValueChange = {
                    cantidad = it.filter { caracter ->
                        caracter.isDigit()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cantidad") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                colors = camposColores()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = cantidadMinima,
                onValueChange = {
                    cantidadMinima = it.filter { caracter ->
                        caracter.isDigit()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Cantidad mínima")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                colors = camposColores()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = ubicacion,
                onValueChange = {
                    ubicacion = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ubicación") },
                placeholder = {
                    Text("Ej: Refrigerador")
                },
                singleLine = true,
                colors = camposColores()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = fechaVencimiento,
                onValueChange = {
                    fechaVencimiento = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Fecha de vencimiento")
                },
                placeholder = {
                    Text("AAAA-MM-DD")
                },
                singleLine = true,
                colors = camposColores()
            )

            if (mensaje.isNotBlank()) {

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mensaje,
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Bold
                )
            }

            if (error.isNotBlank()) {

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = error,
                    color = Color(0xFFF87171)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {

                    val categoria = categoriaSeleccionada
                    val cantidadNumero = cantidad.toIntOrNull()
                    val minimaNumero = cantidadMinima.toIntOrNull()

                    when {

                        nombre.isBlank() -> {
                            error =
                                "Escribe el nombre del producto."
                        }

                        categoria == null -> {
                            error =
                                "Primero crea o selecciona una categoría."
                        }

                        unidadMedida.isBlank() -> {
                            error =
                                "Escribe la unidad de medida."
                        }

                        cantidadNumero == null -> {
                            error =
                                "Escribe una cantidad válida."
                        }

                        minimaNumero == null -> {
                            error =
                                "Escribe una cantidad mínima válida."
                        }

                        ubicacion.isBlank() -> {
                            error =
                                "Escribe la ubicación."
                        }

                        fechaVencimiento.isBlank() -> {
                            error =
                                "Escribe la fecha de vencimiento."
                        }

                        else -> {

                            guardando = true
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

                                    val authorization =
                                        "Bearer $token"

                                    val producto =
                                        RetrofitClient.apiService
                                            .crearProducto(
                                                authorization,
                                                CreateProductoRequest(
                                                    idCategoria =
                                                        categoria.idCategoria,
                                                    nombre =
                                                        nombre.trim(),
                                                    codigoBarras =
                                                        codigoBarras
                                                            .trim()
                                                            .ifBlank {
                                                                null
                                                            },
                                                    unidadMedida =
                                                        unidadMedida.trim()
                                                )
                                            )

                                    RetrofitClient.apiService
                                        .crearInventario(
                                            authorization,
                                            CreateInventarioRequest(
                                                idProducto =
                                                    producto.idProducto,
                                                cantidad =
                                                    cantidadNumero,
                                                cantidadMinima =
                                                    minimaNumero,
                                                fechaVencimiento =
                                                    "${fechaVencimiento.trim()}T00:00:00",
                                                ubicacion =
                                                    ubicacion.trim()
                                            )
                                        )

                                    onProductCreated()

                                } catch (e: HttpException) {

                                    val cuerpo =
                                        e.response()
                                            ?.errorBody()
                                            ?.string()

                                    error =
                                        if (cuerpo.isNullOrBlank()) {
                                            "Error del servidor (${e.code()})."
                                        } else {
                                            "Error (${e.code()}): $cuerpo"
                                        }

                                } catch (e: Exception) {

                                    error =
                                        e.message
                                            ?: "No se pudo registrar el producto."

                                } finally {
                                    guardando = false
                                }
                            }
                        }
                    }
                },
                enabled =
                    !guardando &&
                            !cargandoCategorias &&
                            categoriaSeleccionada != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = verde
                )
            ) {

                if (guardando) {

                    CircularProgressIndicator(
                        color = Color.White
                    )

                } else {

                    Text(
                        text = "Registrar Producto",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun camposColores() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF16A34A),
        unfocusedBorderColor = Color(0xFF475569),
        focusedLabelColor = Color(0xFF22C55E),
        unfocusedLabelColor = Color(0xFF94A3B8),
        cursorColor = Color(0xFF22C55E)
    )