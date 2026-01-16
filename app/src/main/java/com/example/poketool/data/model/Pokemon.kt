package com.example.poketool.data.model

data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String> = emptyList(),
    val imageUrl: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val stats: List<PokemonStatInfo> = emptyList(),
    val abilities: List<String> = emptyList()
) {
    val displayImageUrl: String
        get() = imageUrl ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

    val formattedId: String
        get() = "#${id.toString().padStart(3, '0')}"

    val hasDetails: Boolean
        get() = types.isNotEmpty()

    val hasFullDetails: Boolean
        get() = height != null && weight != null && stats.isNotEmpty()

    val heightInMeters: String
        get() = height?.let { String.format("%.1f m", it / 10.0) } ?: "?"

    val weightInKg: String
        get() = weight?.let { String.format("%.1f kg", it / 10.0) } ?: "?"
}

data class PokemonStatInfo(
    val name: String,
    val baseStat: Int
) {
    val displayName: String
        get() = when (name) {
            "hp" -> "HP"
            "attack" -> "Attack"
            "defense" -> "Defense"
            "special-attack" -> "Sp. Atk"
            "special-defense" -> "Sp. Def"
            "speed" -> "Speed"
            else -> name.replaceFirstChar { it.uppercase() }
        }
}
