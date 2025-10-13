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
    var rounds = 0
        private set

    fun increaseKills() = let { kills++ }

    fun increaseRounds() = let { rounds++ }

    fun gainCoins(amount: Int) = amount.let { coins += it }

    fun costCoins(amount: Int): Boolean {
        if (amount <= coins) {
            coins -= amount
            return true
        } else return false
    }

    fun gainScore(amount: Double) = amount.let { score += (it * difficulty.scoreMultiplier) }
}