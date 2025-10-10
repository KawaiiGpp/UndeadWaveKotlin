package com.akira.undeadwave.config

import com.akira.core.api.config.ConfigFile
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.util.SettingElement

class GlobalSettingsConfig(plugin: UndeadWave) : ConfigFile(plugin, "global_settings") {
    fun load(element: SettingElement<*>) = element.deserialize(config)

    fun save(element: SettingElement<*>) = element.serialize(config)
}