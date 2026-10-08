package com.nexodevs.smartpantry.data.remote

data class ListaCompraResponse(
    val idLista: Int,
    val nombreLista: String,
    val estado: String,
    val fechaCreacion: String
)