package com.akira.undeadwave.game.preset.type

import com.akira.core.api.util.general.EnhancedPredicate
import com.akira.core.api.util.world.deserializeLocationNullable
import com.akira.core.api.util.world.serializeLocation
import com.akira.undeadwave.game.preset.ArenaPresetType
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

class PlayerSpawnpoint : ArenaPresetType<Location>(
    "player_spawnpoint",
    "玩家出生点",
    EnhancedPredicate("") { true }
) {
    override fun serialize(value: Location, config: ConfigurationSection) =
        config.set(name, serializeLocation(value))

    override fun deserialize(config: ConfigurationSection): Location? =
        config.getString(name)?.let { deserializeLocationNullable(it) }
}