package com.akira.undeadwave.main.item.weapon

import com.akira.core.api.util.entity.getFinalMaxHealth
import com.akira.core.api.util.general.launch
import com.akira.core.api.util.math.format
import com.akira.core.api.util.world.ParticlePack
import com.akira.core.api.util.world.SoundPack
import com.akira.undeadwave.UndeadWave
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import kotlin.math.min

class MeleeDamageHandler(
    private val weapon: MeleeWeapon,
    private val data: MeleeDamageData
) {
    fun handleCooldown(): Boolean {
        if (data.sweeping) return false

        if (data.attacker.attackCooldown < 0.85) {
            data.cancelled = true
            data.attacker.sendMessage { Component.text("武器正在冷却，无法攻击。", NamedTextColor.RED) }
            return true
        } else return false
    }

    fun handleAttack(): Boolean {
        val damage = weapon.calculateDamage(data.crit)

        if (data.sweeping) {
            if (!weapon.sweeping) {
                data.cancelled = true
                return true
            }

            val base = damage * weapon.sweepingDamageMultiplier
            val multiplier = 1 + (weapon.sweepDamageBonus / 100.0)

            data.damage = base * multiplier
        } else {
            data.damage = damage
        }

        return false
    }

    fun handleCritParticle() {
        if (!data.crit) return

        val blockData = Material.REDSTONE_BLOCK.createBlockData()
        val particle = ParticlePack(Particle.BLOCK_CRACK, 10, 0.5, 1.0, blockData)
        val sound = SoundPack(Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0F, 0.25F)

        particle.broadcast(data.victim.eyeLocation)
        sound.play(data.attacker)
    }

    fun handleLifeSteal() {
        if (!weapon.lifeStealEnabled) return
        if (!weapon.rollLifeSteal()) return

        val max = data.victim.getFinalMaxHealth()
        val ratio = weapon.lifeStealRatio / 100.0
        val regen = max * ratio

        data.attacker.health = min(regen, data.attacker.getFinalMaxHealth())
        data.attacker.sendMessage {
            Component.text("生命掠夺！", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text("§f你刚刚窃取了 ", NamedTextColor.WHITE))
                .append(Component.text("${regen.format()}♥", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("。", NamedTextColor.WHITE))
        }

        ParticlePack(Particle.HEART, 3, 0.5).broadcast(data.attacker.eyeLocation)
        SoundPack(Sound.ENTITY_ITEM_PICKUP).play(data.attacker)
    }

    fun handleKnockback() {
        UndeadWave.instance.launch {
            if (!data.victim.isValid)
                return@launch

            val knockback = data.victim.velocity.clone()

            knockback.y = 0.0
            knockback.multiply(weapon.knockbackMultiplier)

            data.victim.velocity = knockback
        }
    }
}