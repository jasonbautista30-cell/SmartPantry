package com.nexodevs.smartpantry.data.remote

data class InventarioResponse(
    val idInventario: Int,
    val idProducto: Int,
    val cantidad: Int,
    val cantidadMinima: Int,
    val fechaVencimiento: String?,
    val ubicacion: String?,
    val fechaIngreso: String?,
    val producto: ProductoResponse?
)