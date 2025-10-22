package com.akira.undeadwave.util

import com.akira.core.api.util.entity.MetadataEditor
import com.akira.core.api.util.entity.resetMaxHealthModifiers
import com.akira.core.api.util.entity.setBaseMaxHealth
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.arena.Arena
import com.akira.undeadwave.main.enemy.Enemy
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent

fun Player.restore() {
    resetMaxHealthModifiers()
    setBaseMaxHealth(20.0)
    closeInventory()
    resetTitle()
    sendActionBar(Component.empty())

    inventory.clear()
    arrowsInBody = 0
    exp = 0.0F
    level = 0
    gameMode = GameMode.SURVIVAL
    activePotionEffects.map { it.type }.forEach { removePotionEffect(it) }
    absorptionAmount = 0.0
    health = 20.0
    foodLevel = 20
}

val Entity.arena: Arena?
    get() = MetadataEditor(UndeadWave.instance, this).get("enemy.arena")
        ?.asString()
        ?.run {
            requireNotNull(
                Arena.PresetMap.container.values.firstOrNull { this == it.name }
            ) { "Unknown arena name from entity's metadata: $this (from ${this@arena.type})" }
        }

val Entity.asEnemy: Enemy?
    get() = MetadataEditor(UndeadWave.instance, this).get("enemy.name")
        ?.asString()
        ?.let {
            requireNotNull(
                Enemy.get(it)
            ) { "Unknown enemy name from entity's metadata: $it (from ${this@asEnemy.type})" }
        }

val Entity.lastDamager: Entity?
    get() = (lastDamageCause as? EntityDamageByEntityEvent)?.damager