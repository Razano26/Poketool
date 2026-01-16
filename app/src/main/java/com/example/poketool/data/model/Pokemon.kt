package com.example.poketool.data.model

data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String>
) {
    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

    val formattedId: String
        get() = "#${id.toString().padStart(3, '0')}"
}
