package com.akira.undeadwave.game

import com.akira.core.api.Manager
import com.akira.core.api.util.math.requiresNonNegative
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.config.ArenaPresetConfig

class ArenaPreset(val totalRounds: Int, val enemyMultiplier: Int) {
    init {
        totalRounds.requiresNonNegative()
        enemyMultiplier.requiresNonNegative()
    }

    companion object : Manager<String, ArenaPreset>() {
        val config get() = UndeadWave.instance.configManager.get("ArenaPreset") as ArenaPresetConfig

        fun initializeFromConfig() = config.getAll().forEach(this::register)

        fun saveToConfig() = container.forEach(config::set)
    }
}