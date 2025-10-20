package com.akira.undeadwave.listener

import com.akira.core.api.util.entity.MetadataEditor
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.arena.Arena
import com.akira.undeadwave.main.enemy.Enemy
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
        val entity = event.entity as? Player ?: return
        val metadata = MetadataEditor(UndeadWave.instance, entity).get("enemy.arena")
        val name = metadata?.asString() ?: return

        requireNotNull(
            Arena.PresetMap.container.values.firstOrNull { it.name == name }
        ) { "Unknown arena name from entity's metadata: $name. (EntityType: ${entity.type})" }
            .run { handleEnemyDeath(entity) }

        event.droppedExp = 0
        event.drops.clear()
    }

    @EventHandler
    fun onPlayerDeath(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (event.finalDamage < player.health) return

        val arena = Arena.PlayerMap.get(player) ?: return
        val cause = player.lastDamageCause

        if (cause != null && cause is EntityDamageByEntityEvent) {
            MetadataEditor(UndeadWave.instance, cause.damager).get("enemy.name")
                ?.asString()
                ?.let {
                    val enemy = Enemy.getNonNull(it).displayName
                    player.sendMessage { Component.text("你已被 $enemy 击杀！", NamedTextColor.RED) }
                }
        }

        event.isCancelled = true
        arena.shutdown(false)
    }
}