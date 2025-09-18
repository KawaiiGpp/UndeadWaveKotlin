package com.akira.undeadwave.game

import com.akira.core.api.Manager
import org.bukkit.entity.Player

class ArenaSession(
    val arenaName: String,
    val preset: ArenaPreset,
    val player: Player,
    val spectators: MutableSet<Player>
) {
    var currentRound = 0

    companion object : Manager<String, ArenaSession>() {
        fun fromPlayer(player: Player): ArenaSession? =
            container.values.firstOrNull { it.player == player }

        fun fromSpectator(player: Player): ArenaSession? =
            container.values.firstOrNull { it.spectators.contains(player) }
    }
}