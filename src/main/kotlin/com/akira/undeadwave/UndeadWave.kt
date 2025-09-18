package com.akira.undeadwave

import com.akira.core.api.AkiraPlugin
import com.akira.core.api.config.ConfigManager
import com.akira.undeadwave.config.ArenaPresetConfig
import com.akira.undeadwave.game.ArenaPreset

class UndeadWave : AkiraPlugin() {
    val configManager = ConfigManager()

    companion object {
        lateinit var instance: UndeadWave
            private set
    }

    init {
        instance = this
    }

    override fun onEnable() {
        super.onEnable()

        configManager.register("ArenaPreset", ArenaPresetConfig(this))
        configManager.initializeAll()

        ArenaPreset.initializeFromConfig()
    }

    override fun onDisable() {
        super.onDisable()

        ArenaPreset.saveToConfig()
        configManager.saveAll()
    }
}