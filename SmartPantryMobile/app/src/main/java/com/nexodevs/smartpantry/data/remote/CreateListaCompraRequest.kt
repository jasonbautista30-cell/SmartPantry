package com.nexodevs.smartpantry.data.remote

data class CreateListaCompraRequest(
    val idLista: Int = 0,
    val nombreLista: String,
    val estado: String,
    val fechaCreacion: String
)