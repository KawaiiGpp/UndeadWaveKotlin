package com.akira.undeadwave

import com.akira.core.api.AkiraPlugin
import com.akira.core.api.config.ConfigManager
import com.akira.undeadwave.command.ArenaPresetCommand
import com.akira.undeadwave.command.GlobalSettingsCommand
import com.akira.undeadwave.command.UndeadWaveCommand
import com.akira.undeadwave.config.ArenaPresetConfig
import com.akira.undeadwave.config.EnemyConfig
import com.akira.undeadwave.config.GlobalSettingsConfig
import com.akira.undeadwave.listener.ArenaListener
import com.akira.undeadwave.listener.GeneralListener
import com.akira.undeadwave.main.Global
import com.akira.undeadwave.main.GlobalSettings
import com.akira.undeadwave.main.arena.ArenaPreset
import com.akira.undeadwave.main.enemy.Enemy

class UndeadWave : AkiraPlugin() {
    companion object {
        lateinit var instance: UndeadWave
            private set
    }

    init {
        instance = this
    }

    val configManager = ConfigManager()
    val templatePath = "com/akira/undeadwave/config/template"
    val configArenaPreset get() = configManager.get("ArenaPreset") as ArenaPresetConfig
    val configGlobalSettings get() = configManager.get("GlobalSettings") as GlobalSettingsConfig
    val configEnemy get() = configManager.get("Enemy") as EnemyConfig

    override fun onEnable() {
        super.onEnable()

        configManager.register("ArenaPreset", ArenaPresetConfig(this))
        configManager.register("GlobalSettings", GlobalSettingsConfig(this))
        configManager.register("Enemy", EnemyConfig(this, templatePath))
        configManager.initializeAll()

        GlobalSettings.loadFromConfig()
        ArenaPreset.loadFromConfig()
        Enemy.loadFromConfig()

        Global.selfCheck()

        setupCommand(ArenaPresetCommand(this))
        setupCommand(GlobalSettingsCommand(this))
        setupCommand(UndeadWaveCommand(this))

        setupListener(GeneralListener())
        setupListener(ArenaListener())
    }

    override fun onDisable() {
        super.onDisable()

        ArenaPreset.saveToConfig()
        GlobalSettings.saveToConfig()
        Enemy.saveToConfig()
        configManager.saveAll()
    }
}