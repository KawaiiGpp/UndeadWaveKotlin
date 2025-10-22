package com.akira.undeadwave.listener

import com.akira.undeadwave.main.arena.Arena
import com.akira.undeadwave.util.arena
import com.akira.undeadwave.util.asEnemy
import com.akira.undeadwave.util.lastDamager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent

class ArenaListener : Listener {
    @EventHandler
    fun onEnemyDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity is Player) return

        val arena = entity.arena ?: return
        val enemy = entity.asEnemy ?: return

        if (entity.lastDamager == arena.session.player)
            arena.session.gainCoins(enemy.reward)
        arena.handleEnemyDeath(entity)

        event.droppedExp = 0
        event.drops.clear()
    }

    @EventHandler
    fun onPlayerDamaged(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val arena = Arena.PlayerMap.get(player) ?: return

        val enemy = (event as? EntityDamageByEntityEvent)?.damager?.asEnemy
        enemy?.let { event.damage = it.damage * arena.session.difficulty.enemyDamageMultiplier }

        if (event.finalDamage < player.health) return
        event.isCancelled = true

        enemy?.let { player.sendMessage { Component.text("你已被 ${it.displayName} 击杀！", NamedTextColor.RED) } }
        arena.shutdown(false)
    }
}