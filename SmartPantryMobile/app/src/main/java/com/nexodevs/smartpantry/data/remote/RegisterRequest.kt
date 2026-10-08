package com.nexodevs.smartpantry.data.remote

data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: String? = "Usuario"
)
