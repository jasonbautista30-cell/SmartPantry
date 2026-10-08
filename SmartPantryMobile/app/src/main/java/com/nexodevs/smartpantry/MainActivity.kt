package com.nexodevs.smartpantry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.nexodevs.smartpantry.data.local.SessionManager
import com.nexodevs.smartpantry.data.remote.ListaCompraResponse
import com.nexodevs.smartpantry.data.remote.LoginRequest
import com.nexodevs.smartpantry.data.remote.RetrofitClient
import com.nexodevs.smartpantry.ui.theme.SmartPantryMobileTheme
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SmartPantryMobileTheme {
                SmartPantryApp()
            }
        }
    }
}

@Composable
fun SmartPantryApp() {

    val context = LocalContext.current

    val sessionManager = remember {
        SessionManager(context)
    }

    var isCheckingToken by remember {
        mutableStateOf(sessionManager.isLoggedIn())
    }

    var loggedIn by remember {
        mutableStateOf(sessionManager.isLoggedIn())
    }

    var currentAuthScreen by remember {
        mutableStateOf("login")
    }

    var loginSuccessMessage by remember {
        mutableStateOf("")
    }

    var currentScreen by remember {
        mutableStateOf("dashboard")
    }

    var selectedLista by remember {
        mutableStateOf<ListaCompraResponse?>(null)
    }

    LaunchedEffect(Unit) {
        if (sessionManager.isLoggedIn()) {
            isCheckingToken = true
            try {
                val token = sessionManager.getToken()
                if (!token.isNullOrBlank()) {
                    val user = RetrofitClient.apiService.getCurrentUser("Bearer $token")
                    sessionManager.saveSession(
                        token = token,
                        idUsuario = user.idUsuario,
                        nombre = user.nombre,
                        correo = user.correo,
                        rol = user.rol
                    )
                    loggedIn = true
                } else {
                    sessionManager.clearSession()
                    loggedIn = false
                }
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    sessionManager.clearSession()
                    loggedIn = false
                } else {
                    loggedIn = true
                }
            } catch (_: Exception) {
                loggedIn = true
            } finally {
                isCheckingToken = false
            }
        } else {
            isCheckingToken = false
        }
    }

    if (isCheckingToken) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🥗", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Smart Pantry",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(color = Color(0xFF16A34A))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Verificando sesión...",
                    color = Color(0xFF94A3B8)
                )
            }
        }

    } else if (!loggedIn) {

        if (currentAuthScreen == "register") {

            BackHandler {
                currentAuthScreen = "login"
            }

            RegisterScreen(
                sessionManager = sessionManager,
                onRegisterSuccessWithSession = {
                    loggedIn = true
                    currentScreen = "dashboard"
                },
                onRegisterSuccessNeedLogin = { msg ->
                    loginSuccessMessage = msg
                    currentAuthScreen = "login"
                },
                onBackToLogin = {
                    currentAuthScreen = "login"
                }
            )

        } else {

            LoginScreen(
                sessionManager = sessionManager,
                initialMessage = loginSuccessMessage,
                onLoginSuccess = {
                    loggedIn = true
                    currentScreen = "dashboard"
                },
                onGoToRegister = {
                    loginSuccessMessage = ""
                    currentAuthScreen = "register"
                }
            )
        }

    } else {

        BackHandler(enabled = currentScreen != "dashboard") {
            currentScreen = when (currentScreen) {
                "shoppingDetail" -> "shoppingList"
                else -> "dashboard"
            }
        }

        when (currentScreen) {

            "inventory" -> {

                InventoryScreen(
                    sessionManager = sessionManager,
                    onBack = {
                        currentScreen = "dashboard"
                    }
                )
            }

            "addProduct" -> {

                AddProductScreen(
                    sessionManager = sessionManager,
                    onBack = {
                        currentScreen = "dashboard"
                    },
                    onProductCreated = {
                        currentScreen = "inventory"
                    }
                )
            }

            "shoppingList" -> {

                ShoppingListScreen(
                    sessionManager = sessionManager,
                    onBack = {
                        currentScreen = "dashboard"
                    },
                    onSelectList = { lista ->
                        selectedLista = lista
                        currentScreen = "shoppingDetail"
                    }
                )
            }

            "shoppingDetail" -> {

                val lista = selectedLista
                if (lista != null) {

                    ShoppingDetailScreen(
                        sessionManager = sessionManager,
                        lista = lista,
                        onBack = {
                            currentScreen = "shoppingList"
                        }
                    )

                } else {

                    currentScreen = "shoppingList"
                }
            }

            else -> {

                DashboardScreen(
                    sessionManager = sessionManager,

                    onLogout = {
                        sessionManager.clearSession()
                        loggedIn = false
                        currentScreen = "dashboard"
                    },

                    onOpenInventory = {
                        currentScreen = "inventory"
                    },

                    onAddProduct = {
                        currentScreen = "addProduct"
                    },

                    onOpenShoppingList = {
                        currentScreen = "shoppingList"
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    sessionManager: SessionManager,
    initialMessage: String = "",
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit = {}
) {

    var correo by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var mostrarPassword by remember {
        mutableStateOf(false)
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf("")
    }

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
                .background(
                    color = tarjeta,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "🥗",
                fontSize = 56.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Smart Pantry",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "ESFE-ÁGAPE • NEXO DEVS",
                color = textoSecundario,
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "Iniciar sesión",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Accede a tu despensa inteligente",
                modifier = Modifier.fillMaxWidth(),
                color = textoSecundario
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            OutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Correo electrónico")
                },
                placeholder = {
                    Text("tucorreo@ejemplo.com")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = verde,
                    focusedLabelColor = verde,
                    unfocusedLabelColor = textoSecundario,
                    cursorColor = verde
                )
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Contraseña")
                },
                singleLine = true,

                visualTransformation = if (mostrarPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },

                trailingIcon = {

                    TextButton(
                        onClick = {
                            mostrarPassword = !mostrarPassword
                        }
                    ) {

                        Text(
                            text = if (mostrarPassword) {
                                "Ocultar"
                            } else {
                                "Mostrar"
                            },
                            color = verde
                        )
                    }
                },

                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = verde,
                    focusedLabelColor = verde,
                    unfocusedLabelColor = textoSecundario,
                    cursorColor = verde
                )
            )

            if (initialMessage.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(
                    text = initialMessage,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF22C55E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (error.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(
                    text = error,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF87171),
                    fontSize = 14.sp
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {

                    if (
                        correo.isBlank() ||
                        password.isBlank()
                    ) {

                        error =
                            "Completa el correo y la contraseña."

                        return@Button
                    }

                    loading = true
                    error = ""

                    scope.launch {

                        try {

                            val response =
                                RetrofitClient.apiService.login(
                                    LoginRequest(
                                        correo = correo.trim(),
                                        password = password
                                    )
                                )

                            sessionManager.saveSession(
                                token = response.token,
                                idUsuario = response.idUsuario,
                                nombre = response.nombre,
                                correo = response.correo,
                                rol = response.rol
                            )

                            onLoginSuccess()

                        } catch (e: HttpException) {

                            error = when (e.code()) {

                                400 ->
                                    "Revisa los datos ingresados."

                                401 ->
                                    "Correo o contraseña incorrectos."

                                404 ->
                                    "Usuario no encontrado."

                                else ->
                                    "Error del servidor (${e.code()})."
                            }

                        } catch (e: IOException) {

                            error =
                                "No se pudo conectar con Smart Pantry."

                        } catch (e: Exception) {

                            error =
                                e.message
                                    ?: "Ocurrió un error inesperado."

                        } finally {

                            loading = false
                        }
                    }
                },

                enabled = !loading,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape = RoundedCornerShape(12.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = verde
                )
            ) {

                if (loading) {

                    CircularProgressIndicator(
                        color = Color.White
                    )

                } else {

                    Text(
                        text = "Iniciar sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    color = textoSecundario,
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = onGoToRegister
                ) {
                    Text(
                        text = "Crear cuenta",
                        color = verde,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Smart Pantry © 2026 • NEXO DEVS",
                color = textoSecundario,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun DashboardScreen(
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    onOpenInventory: () -> Unit,
    onAddProduct: () -> Unit,
    onOpenShoppingList: () -> Unit
) {

    val scope = rememberCoroutineScope()

    var checkingSession by remember {
        mutableStateOf(false)
    }

    var sessionMessage by remember {
        mutableStateOf("")
    }

    val fondo = Color(0xFF0F172A)
    val tarjeta = Color(0xFF1E293B)
    val verde = Color(0xFF16A34A)
    val azul = Color(0xFF60A5FA)
    val textoSecundario = Color(0xFF94A3B8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .padding(24.dp)
    ) {

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = "🥗 Smart Pantry",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "ESFE-ÁGAPE • NEXO DEVS",
                    color = textoSecundario,
                    fontSize = 11.sp
                )
            }

            TextButton(
                onClick = onLogout
            ) {

                Text(
                    text = "Salir",
                    color = Color(0xFFF87171)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "Mi Despensa",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Gestión inteligente de alimentos e inventario",
            color = textoSecundario
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = tarjeta,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(22.dp)
        ) {

            Text(
                text = "Sesión activa",
                color = verde,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = sessionManager.getName(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = sessionManager.getEmail(),
                color = textoSecundario
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Rol: ${sessionManager.getRole()}",
                color = textoSecundario
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            HorizontalDivider(
                color = Color(0xFF334155)
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Button(
                onClick = {

                    checkingSession = true
                    sessionMessage = ""

                    scope.launch {

                        try {

                            val token =
                                sessionManager.getToken()

                            if (token.isNullOrBlank()) {

                                sessionMessage =
                                    "No hay token almacenado."

                                checkingSession = false

                                return@launch
                            }

                            val user =
                                RetrofitClient.apiService
                                    .getCurrentUser(
                                        "Bearer $token"
                                    )

                            sessionMessage =
                                "JWT válido ✅\nUsuario confirmado: ${user.nombre}"

                        } catch (e: HttpException) {

                            sessionMessage =
                                if (e.code() == 401) {

                                    "La sesión expiró o el token no es válido."

                                } else {

                                    "Error del servidor (${e.code()})."
                                }

                        } catch (e: Exception) {

                            sessionMessage =
                                "Error: ${
                                    e.message ?: "desconocido"
                                }"

                        } finally {

                            checkingSession = false
                        }
                    }
                },

                modifier = Modifier.fillMaxWidth(),

                enabled = !checkingSession,

                colors = ButtonDefaults.buttonColors(
                    containerColor = verde
                )
            ) {

                if (checkingSession) {

                    CircularProgressIndicator(
                        color = Color.White
                    )

                } else {

                    Text(
                        text = "Comprobar sesión JWT"
                    )
                }
            }

            if (sessionMessage.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = sessionMessage,

                    color = if (
                        sessionMessage.contains("✅")
                    ) {

                        verde

                    } else {

                        Color(0xFFF87171)
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        Text(
            text = "Acciones rápidas",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        DashboardOption(
            title = "Registrar Nuevo Producto",
            icon = "➕",
            color = verde,
            onClick = onAddProduct
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        DashboardOption(
            title = "Ver Inventario Completo",
            icon = "📦",
            color = azul,
            onClick = onOpenInventory
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        DashboardOption(
            title = "Gestión de Lista de Compras",
            icon = "🛒",
            color = Color(0xFFA78BFA),
            onClick = onOpenShoppingList
        )
    }
}

@Composable
fun DashboardOption(
    title: String,
    icon: String,
    color: Color,
    onClick: () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable {
                onClick()
            }
            .padding(18.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = icon,
            fontSize = 22.sp
        )

        Text(
            text = title,
            modifier = Modifier.padding(
                start = 14.dp
            ),
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}