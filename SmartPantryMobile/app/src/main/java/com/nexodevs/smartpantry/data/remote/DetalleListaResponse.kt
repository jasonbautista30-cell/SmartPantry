package com.nexodevs.smartpantry.data.remote

import com.google.gson.annotations.SerializedName

data class DetalleListaResponse(
    val idDetalle: Int = 0,
    val idLista: Int = 0,
    val idProducto: Int = 0,
    @SerializedName("cantidadAComprar", alternate = ["cantidadComprar"])
    val cantidadAComprar: Int = 0,
    val comprado: Boolean = false,
    val producto: ProductoResponse? = null
)
