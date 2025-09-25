package com.akira.undeadwave.game.preset.type

import com.akira.core.api.util.general.EnhancedPredicate
import com.akira.undeadwave.game.preset.ArenaPresetType
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection

class ArenaWorld : ArenaPresetType<World>(
    "world",
    "世界",
    EnhancedPredicate("") { true }
) {
    override fun serialize(value: World, config: ConfigurationSection) =
        config.set(name, value)

    override fun deserialize(config: ConfigurationSection): World? =
        config.getString(name)?.let { Bukkit.getWorld(it) }
}