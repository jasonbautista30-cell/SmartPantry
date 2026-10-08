package com.nexodevs.smartpantry.data.remote

data class UserProfileResponse(
    val idUsuario: Int,
    val nombre: String,
    val correo: String,
    val rol: String
)