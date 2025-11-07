package com.akira.undeadwave.main.item.weapon

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class MeleeDamageData(
    val attacker: Player,
    val victim: LivingEntity,
    val sweeping: Boolean
) {
    var damage: Double = 0.0
    var crit: Boolean = false
    var trueDamage: Boolean = false
    var cancelled: Boolean = false
}