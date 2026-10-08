package com.nexodevs.smartpantry.data.remote

data class CreateProductoRequest(
    val idCategoria: Int,
    val nombre: String,
    val codigoBarras: String?,
    val unidadMedida: String
)