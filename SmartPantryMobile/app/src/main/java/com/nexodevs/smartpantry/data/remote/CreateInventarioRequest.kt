package com.nexodevs.smartpantry.data.remote

data class CreateInventarioRequest(
    val idProducto: Int,
    val cantidad: Int,
    val cantidadMinima: Int,
    val fechaVencimiento: String?,
    val ubicacion: String
)