package com.nexodevs.smartpantry.data.remote

import com.google.gson.annotations.SerializedName

data class CreateDetalleListaRequest(
    val idLista: Int,
    val idProducto: Int,
    @SerializedName("cantidadAComprar", alternate = ["cantidadComprar"])
    val cantidadAComprar: Int,
    val comprado: Boolean = false
)
