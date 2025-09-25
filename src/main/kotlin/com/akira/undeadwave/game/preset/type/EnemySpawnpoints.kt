package com.akira.undeadwave.game.preset.type

import com.akira.core.api.util.general.EnhancedPredicate
import com.akira.core.api.util.world.deserializeLocation
import com.akira.core.api.util.world.serializeLocation
import com.akira.undeadwave.game.preset.ArenaPresetType
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

class EnemySpawnpoints : ArenaPresetType<List<Location>>(
    "enemy_spawnpoints",
    "怪物刷新点",
    EnhancedPredicate("至少指定一个。") { it.isNotEmpty() }
) {
    override fun serialize(value: List<Location>, config: ConfigurationSection) =
        config.set(name, value.map { serializeLocation(it) })

    override fun deserialize(config: ConfigurationSection): List<Location>? =
        config.getStringList(name).map { deserializeLocation(it) }
}