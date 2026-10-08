package com.nexodevs.smartpantry.data.remote

data class AuthResponse(
    val token: String,
    val idUsuario: Int,
    val nombre: String,
    val correo: String,
    val rol: String,
    val expiration: String
)