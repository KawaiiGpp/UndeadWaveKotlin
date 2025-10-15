package com.akira.undeadwave.main.arena

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.*

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
    var round = 0
        private set

    private val markedEnemies = mutableSetOf<UUID>()
    val enemies get() = markedEnemies.toSet()

    fun increaseKills() {
        kills++
    }

    fun increaseRound() {
        round++
    }

    fun gainCoins(amount: Int) = amount.let { coins += it }

    fun costCoins(amount: Int): Boolean {
        if (amount <= coins) {
            coins -= amount
            return true
        } else return false
    }

    fun gainScore(amount: Double) = amount.let { score += (it * difficulty.scoreMultiplier) }

    fun isMarked(entity: LivingEntity) = markedEnemies.contains(entity.uniqueId)

    fun hasEnemiesLeft() = markedEnemies.isNotEmpty()

    fun markEnemy(entity: LivingEntity) {
        require(!this.isMarked(entity)) { "Enemy already marked." }
        markedEnemies.add(entity.uniqueId)
    }

    fun unmarkEnemy(entity: LivingEntity) {
        require(this.isMarked(entity)) { "Enemy not marked." }
        markedEnemies.remove(entity.uniqueId)
    }
}