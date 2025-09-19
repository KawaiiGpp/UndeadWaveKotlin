package com.akira.undeadwave.config

import com.akira.core.api.config.ConfigFile
import com.akira.core.api.config.getLocationList
import com.akira.core.api.config.getWorld
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.game.ArenaPreset

class ArenaPresetConfig(plugin: UndeadWave) : ConfigFile(plugin, "arena_preset") {
    fun get(arenaName: String): ArenaPreset {
        require(this.contains(arenaName)) { "Arena $arenaName doesn't exist." }

        val totalRounds = config.getInt("$arenaName.total_rounds")
        val enemyMultiplier = config.getInt("$arenaName.enemy_multiplier")

        fun <T : Any> check(value: T?, field: String): T =
            requireNotNull(value) { "$field for $arenaName not specified or cannot be loaded." }

        val world = check(config.getWorld("$arenaName.world"), "World")
        val spawnpoint = check(config.getLocation("$arenaName.spawnpoint"), "Spawnpoint")
        val enemySpawnpoints = check(config.getLocationList("$arenaName.enemy.spawnpoints"), "Enemy Spawnpoints")

        return ArenaPreset(totalRounds, enemyMultiplier, world, spawnpoint, enemySpawnpoints)
    }

    fun getAll(): Map<String, ArenaPreset> {
        val map = mutableMapOf<String, ArenaPreset>()

        config.getKeys(false).forEach { map[it] = this.get(it) }
        return map
    }

    fun set(arenaName: String, preset: ArenaPreset) {
        config.set("$arenaName.total_rounds", preset.totalRounds)
        config.set("$arenaName.enemy_multiplier", preset.enemyMultiplier)
        config.set("$arenaName.world", preset.world)
        config.set("$arenaName.spawnpoint", preset.spawnpoint)

        preset.enemySpawnpoints.forEachIndexed { i, loc -> config.set("$arenaName.enemy.spawnpoints.$i", loc) }
    }

    fun setAll(map: Map<String, ArenaPreset>) = map.forEach(this::set)

    fun remove(arenaName: String) = config.set(arenaName, null)

    fun removeAll() = config.getKeys(false).forEach(this::remove)

    fun contains(arenaName: String): Boolean = config.contains(arenaName)
}