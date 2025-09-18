package com.akira.undeadwave.config

import com.akira.core.api.config.ConfigFile
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.game.ArenaPreset

class ArenaPresetConfig(plugin: UndeadWave) : ConfigFile(plugin, "arena_preset") {
    fun get(arenaName: String): ArenaPreset {
        require(this.has(arenaName)) { "Arena $arenaName doesn't exist." }

        val totalRounds = config.getInt("$arenaName.total_rounds")
        val enemyMultiplier = config.getInt("$arenaName.enemy_multiplier")

        return ArenaPreset(totalRounds, enemyMultiplier)
    }

    fun getAll(): Map<String, ArenaPreset> {
        val map = mutableMapOf<String, ArenaPreset>()

        config.getKeys(false).forEach { map[it] = this.get(it) }
        return map
    }

    fun set(arenaName: String, preset: ArenaPreset) {
        config.set("$arenaName.total_rounds", preset.totalRounds)
        config.set("$arenaName.enemy_multiplier", preset.enemyMultiplier)
    }

    fun has(arenaName: String): Boolean = config.contains(arenaName)

    fun remove(arenaName: String) = config.set(arenaName, null)
}