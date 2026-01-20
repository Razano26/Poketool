package com.example.poketool.data.model

data class Team(
    val id: Long = 0,
    val name: String,
    val pokemon: List<Pokemon> = emptyList()
) {
    val isFull: Boolean
        get() = pokemon.size >= MAX_SIZE

    val isEmpty: Boolean
        get() = pokemon.isEmpty()

    val size: Int
        get() = pokemon.size

    fun contains(pokemonId: Int): Boolean {
        return pokemon.any { it.id == pokemonId }
    }

    val typeCount: Map<String, Int>
        get() = pokemon
            .flatMap { it.types }
            .groupingBy { it }
            .eachCount()

    val typeCoverage: TypeCoverage
        get() = TypeCoverage.calculate(pokemon.flatMap { it.types })

    val totalStats: TeamStats
        get() = TeamStats.calculate(pokemon)

    companion object {
        const val MAX_SIZE = 6
    }
}

data class TypeCoverage(
    val strengths: Map<String, Int>,
    val weaknesses: Map<String, Int>,
    val immunities: Set<String>
) {
    companion object {
        private val typeChart = mapOf(
            "Normal" to TypeEffectiveness(
                weakTo = listOf("Fighting"),
                immuneTo = listOf("Ghost")
            ),
            "Fire" to TypeEffectiveness(
                weakTo = listOf("Water", "Ground", "Rock"),
                resistantTo = listOf("Fire", "Grass", "Ice", "Bug", "Steel", "Fairy")
            ),
            "Water" to TypeEffectiveness(
                weakTo = listOf("Electric", "Grass"),
                resistantTo = listOf("Fire", "Water", "Ice", "Steel")
            ),
            "Electric" to TypeEffectiveness(
                weakTo = listOf("Ground"),
                resistantTo = listOf("Electric", "Flying", "Steel")
            ),
            "Grass" to TypeEffectiveness(
                weakTo = listOf("Fire", "Ice", "Poison", "Flying", "Bug"),
                resistantTo = listOf("Water", "Electric", "Grass", "Ground")
            ),
            "Ice" to TypeEffectiveness(
                weakTo = listOf("Fire", "Fighting", "Rock", "Steel"),
                resistantTo = listOf("Ice")
            ),
            "Fighting" to TypeEffectiveness(
                weakTo = listOf("Flying", "Psychic", "Fairy"),
                resistantTo = listOf("Bug", "Rock", "Dark")
            ),
            "Poison" to TypeEffectiveness(
                weakTo = listOf("Ground", "Psychic"),
                resistantTo = listOf("Grass", "Fighting", "Poison", "Bug", "Fairy")
            ),
            "Ground" to TypeEffectiveness(
                weakTo = listOf("Water", "Grass", "Ice"),
                resistantTo = listOf("Poison", "Rock"),
                immuneTo = listOf("Electric")
            ),
            "Flying" to TypeEffectiveness(
                weakTo = listOf("Electric", "Ice", "Rock"),
                resistantTo = listOf("Grass", "Fighting", "Bug"),
                immuneTo = listOf("Ground")
            ),
            "Psychic" to TypeEffectiveness(
                weakTo = listOf("Bug", "Ghost", "Dark"),
                resistantTo = listOf("Fighting", "Psychic")
            ),
            "Bug" to TypeEffectiveness(
                weakTo = listOf("Fire", "Flying", "Rock"),
                resistantTo = listOf("Grass", "Fighting", "Ground")
            ),
            "Rock" to TypeEffectiveness(
                weakTo = listOf("Water", "Grass", "Fighting", "Ground", "Steel"),
                resistantTo = listOf("Normal", "Fire", "Poison", "Flying")
            ),
            "Ghost" to TypeEffectiveness(
                weakTo = listOf("Ghost", "Dark"),
                resistantTo = listOf("Poison", "Bug"),
                immuneTo = listOf("Normal", "Fighting")
            ),
            "Dragon" to TypeEffectiveness(
                weakTo = listOf("Ice", "Dragon", "Fairy"),
                resistantTo = listOf("Fire", "Water", "Electric", "Grass")
            ),
            "Dark" to TypeEffectiveness(
                weakTo = listOf("Fighting", "Bug", "Fairy"),
                resistantTo = listOf("Ghost", "Dark"),
                immuneTo = listOf("Psychic")
            ),
            "Steel" to TypeEffectiveness(
                weakTo = listOf("Fire", "Fighting", "Ground"),
                resistantTo = listOf("Normal", "Grass", "Ice", "Flying", "Psychic", "Bug", "Rock", "Dragon", "Steel", "Fairy"),
                immuneTo = listOf("Poison")
            ),
            "Fairy" to TypeEffectiveness(
                weakTo = listOf("Poison", "Steel"),
                resistantTo = listOf("Fighting", "Bug", "Dark"),
                immuneTo = listOf("Dragon")
            )
        )

        fun calculate(types: List<String>): TypeCoverage {
            val weaknesses = mutableMapOf<String, Int>()
            val resistances = mutableMapOf<String, Int>()
            val immunities = mutableSetOf<String>()

            types.forEach { type ->
                val effectiveness = typeChart[type] ?: return@forEach
                effectiveness.weakTo.forEach { w ->
                    weaknesses[w] = (weaknesses[w] ?: 0) + 1
                }
                effectiveness.resistantTo.forEach { r ->
                    resistances[r] = (resistances[r] ?: 0) + 1
                }
                immunities.addAll(effectiveness.immuneTo)
            }

            return TypeCoverage(
                strengths = resistances,
                weaknesses = weaknesses.filterKeys { it !in immunities },
                immunities = immunities
            )
        }
    }

    private data class TypeEffectiveness(
        val weakTo: List<String> = emptyList(),
        val resistantTo: List<String> = emptyList(),
        val immuneTo: List<String> = emptyList()
    )
}

data class TeamStats(
    val totalHp: Int,
    val totalAttack: Int,
    val totalDefense: Int,
    val totalSpAtk: Int,
    val totalSpDef: Int,
    val totalSpeed: Int,
    val averageHp: Int,
    val averageAttack: Int,
    val averageDefense: Int,
    val averageSpAtk: Int,
    val averageSpDef: Int,
    val averageSpeed: Int
) {
    companion object {
        fun calculate(pokemon: List<Pokemon>): TeamStats {
            if (pokemon.isEmpty()) {
                return TeamStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            }

            var hp = 0
            var atk = 0
            var def = 0
            var spAtk = 0
            var spDef = 0
            var speed = 0

            pokemon.forEach { p ->
                p.stats.forEach { stat ->
                    when (stat.name) {
                        "hp" -> hp += stat.baseStat
                        "attack" -> atk += stat.baseStat
                        "defense" -> def += stat.baseStat
                        "special-attack" -> spAtk += stat.baseStat
                        "special-defense" -> spDef += stat.baseStat
                        "speed" -> speed += stat.baseStat
                    }
                }
            }

            val count = pokemon.size
            return TeamStats(
                totalHp = hp,
                totalAttack = atk,
                totalDefense = def,
                totalSpAtk = spAtk,
                totalSpDef = spDef,
                totalSpeed = speed,
                averageHp = hp / count,
                averageAttack = atk / count,
                averageDefense = def / count,
                averageSpAtk = spAtk / count,
                averageSpDef = spDef / count,
                averageSpeed = speed / count
            )
        }
    }
}
