package com.nexodevs.smartpantry.data.remote

data class ProductoResponse(
    val idProducto: Int,
    val idCategoria: Int,
    val nombre: String,
    val codigoBarras: String?,
    val unidadMedida: String?,
    val categoria: CategoriaResponse?
)