package com.akira.undeadwave

import com.akira.core.api.AkiraPlugin
import com.akira.core.api.config.ConfigManager
import com.akira.undeadwave.command.ArenaPresetCommand
import com.akira.undeadwave.command.GlobalSettingsCommand
import com.akira.undeadwave.config.ArenaPresetConfig
import com.akira.undeadwave.config.GlobalSettingsConfig
import com.akira.undeadwave.listener.ArenaPresetResetListener
import com.akira.undeadwave.main.Global
import com.akira.undeadwave.main.GlobalSettings
import com.akira.undeadwave.main.arena.ArenaPreset

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
    val configGlobalSettings get() = configManager.get("GlobalSettings") as GlobalSettingsConfig

    override fun onEnable() {
        super.onEnable()

        configManager.register("ArenaPreset", ArenaPresetConfig(this))
        configManager.register("GlobalSettings", GlobalSettingsConfig(this))
        configManager.initializeAll()

        GlobalSettings.loadFromConfig()
        ArenaPreset.loadFromConfig()

        Global.performSelfCheck()

        setupCommand(ArenaPresetCommand(this))
        setupCommand(GlobalSettingsCommand(this))

        setupListener(ArenaPresetResetListener())
    }

    override fun onDisable() {
        super.onDisable()

        ArenaPreset.saveToConfig()
        GlobalSettings.saveToConfig()
        configManager.saveAll()
    }
}