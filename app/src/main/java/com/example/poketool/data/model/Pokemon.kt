package com.example.poketool.data.model

data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String> = emptyList(),
    val imageUrl: String? = null
) {
    val displayImageUrl: String
        get() = imageUrl ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

    val formattedId: String
        get() = "#${id.toString().padStart(3, '0')}"

    val hasDetails: Boolean
        get() = types.isNotEmpty()
}
