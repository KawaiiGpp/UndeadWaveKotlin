package com.akira.undeadwave

import com.akira.core.api.AkiraPlugin
import com.akira.core.api.config.ConfigManager
import com.akira.undeadwave.command.ArenaPresetCommand
import com.akira.undeadwave.config.ArenaPresetConfig
import com.akira.undeadwave.listener.ArenaPresetResetListener
import com.akira.undeadwave.main.preset.ArenaPreset

class UndeadWave : AkiraPlugin() {
    companion object {
        lateinit var instance: UndeadWave
            private set
    }

    init {
        instance = this
    }

    val configManager = ConfigManager()
    val configArenaPreset get() = configManager.get("ArenaPreset") as ArenaPresetConfig

    override fun onEnable() {
        super.onEnable()

        configManager.register("ArenaPreset", ArenaPresetConfig(this))
        configManager.initializeAll()

        ArenaPreset.loadFromConfig()

        setupCommand(ArenaPresetCommand(this))
        setupListener(ArenaPresetResetListener())
    }

    override fun onDisable() {
        super.onDisable()

        ArenaPreset.saveToConfig()
        configManager.saveAll()
    }
}