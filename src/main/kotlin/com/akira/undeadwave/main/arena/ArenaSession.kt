package com.akira.undeadwave.main.arena

import org.bukkit.entity.Player

class ArenaSession(
    val player: Player,
    val difficulty: ArenaDifficulty
) {
    var kills = 0
    var coins = 0
    var score = 0
}