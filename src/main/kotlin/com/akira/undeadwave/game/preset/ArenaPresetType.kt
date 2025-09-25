package com.akira.undeadwave.game.preset

import com.akira.core.api.Manager
import com.akira.core.api.config.ConfigSerializer
import com.akira.core.api.util.general.EnhancedPredicate
import com.akira.undeadwave.game.preset.type.*

abstract class ArenaPresetType<T : Any>(
    val name: String,
    val displayName: String,
    val predicate: EnhancedPredicate<T>
) : ConfigSerializer<T> {
    companion object : Manager<String, ArenaPresetType<*>>() {
        val TOTAL_ROUNDS = TotalRounds()
        val ENEMY_MULTIPLIER = EnemyMultiplier()
        val WORLD = ArenaWorld()
        val PLAYER_SPAWNPOINT = PlayerSpawnpoint()
        val ENEMY_SPAWNPOINTS = EnemySpawnpoints()

        init {
            register(TOTAL_ROUNDS)
            register(ENEMY_MULTIPLIER)
            register(WORLD)
            register(PLAYER_SPAWNPOINT)
            register(ENEMY_SPAWNPOINTS)
        }

        fun register(type: ArenaPresetType<*>) = register(type.name, type)
    }
}