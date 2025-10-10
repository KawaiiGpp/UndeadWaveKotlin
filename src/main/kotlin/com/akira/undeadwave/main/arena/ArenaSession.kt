package com.akira.undeadwave.main.arena

import org.bukkit.entity.Player

class ArenaSession(
    val player: Player,
    val difficulty: ArenaDifficulty
) {
    var kills = 0
        private set
    var coins = 0
        private set
    var score = 0.0
        private set

    fun increaseKills() = let { kills += 1 }

    fun gainCoins(amount: Int) = amount.let { coins += it }

    fun costCoins(amount: Int): Boolean {
        if (amount <= coins) {
            coins -= amount
            return true
        } else return false
    }

    fun gainScore(amount: Double) = amount.let { score += (it * difficulty.scoreMultiplier) }
}