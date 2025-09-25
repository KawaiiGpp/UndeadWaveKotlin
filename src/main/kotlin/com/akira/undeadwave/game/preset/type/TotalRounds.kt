package com.akira.undeadwave.game.preset.type

import com.akira.core.api.util.general.EnhancedPredicate
import com.akira.undeadwave.game.preset.ArenaPresetType
import org.bukkit.configuration.ConfigurationSection

class TotalRounds : ArenaPresetType<Int>(
    "total_rounds",
    "总回合数",
    EnhancedPredicate("必须为正整数。") { it > 0 }
) {
    override fun serialize(value: Int, config: ConfigurationSection) =
        config.set(name, value)

    override fun deserialize(config: ConfigurationSection): Int? =
        config.getInt(name)
}