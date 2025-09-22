package com.akira.undeadwave.game

import com.akira.core.api.Manager
import com.akira.core.api.util.math.requiresNonNegative
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.config.ArenaPresetConfig
import org.bukkit.Location
import org.bukkit.World

class ArenaPreset(
    val totalRounds: Int,
    val enemyMultiplier: Int,
    val world: World,
    val spawnpoint: Location,
    val enemySpawnpoints: List<Location>
) {
    init {
        totalRounds.requiresNonNegative()
        enemyMultiplier.requiresNonNegative()

        require(enemySpawnpoints.isNotEmpty()) { "At least 1 spawnpoint for enemies is required." }
        require(enemySpawnpoints.all { world == it.world }) { "Incorrect world from enemy spawnpoints." }
        require(world == spawnpoint.world) { "Incorrect world from spawnpoint." }
    }

    companion object : Manager<String, ArenaPreset>() {
        val config get() = UndeadWave.instance.configManager.get("ArenaPreset") as ArenaPresetConfig

        override fun register(key: String, element: ArenaPreset) {
            require(isWorldFree(element.world)) { "World ${element.world.name} already registered." }
            super.register(key, element)
        }

        fun loadFromConfig() = clear().also { config.getAll().forEach(this::register) }

        fun saveToConfig() = config.removeAll().also { config.setAll(container) }

        fun isWorldRegistered(world: World): Boolean = container.any { it.value.world == world }

        fun isWorldFree(world: World): Boolean = container.all { it.value.world != world }
    }
}