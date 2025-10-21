package com.akira.undeadwave.util

import com.akira.core.api.util.entity.resetMaxHealthModifiers
import com.akira.core.api.util.entity.setBaseMaxHealth
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

val Entity.lastDamager get() = (lastDamageCause as? EntityDamageByEntityEvent)?.damager