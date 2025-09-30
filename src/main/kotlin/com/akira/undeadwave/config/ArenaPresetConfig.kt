package com.akira.undeadwave.config

import com.akira.core.api.config.ConfigFile
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.preset.ArenaPreset

class ArenaPresetConfig(plugin: UndeadWave) : ConfigFile(plugin, "arena_preset") {
    fun get(name: String): ArenaPreset? {
        val section = config.getConfigurationSection(name) ?: return null

        return ArenaPreset(name).apply { elements.forEach { it.value.deserialize(section) } }
    }

    fun load(name: String): ArenaPreset =
        requireNotNull(this.get(name)) { "Failed loading Arena Preset $name from config." }

    fun loadAll(): List<ArenaPreset> {
        val presets = mutableListOf<ArenaPreset>()

        config.getKeys(false).forEach { presets.add(this.load(it)) }
        return presets
    }

    fun save(preset: ArenaPreset) {
        val name = preset.name
        val section = config.getConfigurationSection(name) ?: config.createSection(name)

        preset.elements.forEach { it.value.serialize(section) }
    }

    fun saveAll(presets: Collection<ArenaPreset>) = presets.forEach { this.save(it) }
}