package com.akira.undeadwave.main.item.weapon

import com.akira.core.api.util.entity.MetadataEditor
import com.akira.core.api.util.general.randomSublist
import com.akira.core.api.util.world.ParticlePack
import com.akira.core.api.util.world.SoundPack
import com.akira.core.api.util.world.worldNonNull
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.arena.Arena
import com.akira.undeadwave.util.asEnemy
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class RangedWeaponBullet(val owner: Player, val weapon: RangedWeapon) {
    val beginning = owner.location
    val direction = beginning.direction

    fun launch() {
        var multiplier = 0.0
        val damagedEnemies = mutableListOf<LivingEntity>()
        var hitCounter = 0

        while (true) {
            if (multiplier + weapon.stepLength > weapon.distance) break
            multiplier += weapon.stepLength

            if (hitCounter >= weapon.maxTargetAmount) break

            val currentDirection = direction.clone().multiply(multiplier)
            val targetLocation = beginning.clone().add(currentDirection)

            if (canStopProjectiles(targetLocation.block)) break
            if (multiplier >= 2) weapon.particle.broadcast(targetLocation)

            var enemies = hit(targetLocation) - damagedEnemies
            if (enemies.isEmpty()) continue
            enemies = limitTargetAmount(enemies, hitCounter)

            handleAttack(enemies)
            hitCounter += enemies.size

            if (!weapon.repeatHit) damagedEnemies += enemies
            if (!weapon.piercing) break
        }
    }

    private fun hit(location: Location): List<LivingEntity> {
        val radius = (weapon.stepLength / 2) * weapon.hitboxMultiplier

        return location.worldNonNull.getNearbyLivingEntities(location, radius)
            .asSequence()
            .filter { it.isValid }
            .filter { it !is Player }
            .filter { !it.isDead }
            .filter { it.asEnemy != null }
            .toList()
    }

    private fun canStopProjectiles(block: Block): Boolean {
        if (block.type.isSolid && block.type.isOccluding)
            return true

        val boxes = block.collisionShape.boundingBoxes
        return boxes.any { it.widthX == 1.0 && it.widthZ == 1.0 && it.height == 1.0 }
    }

    private fun damage(entity: LivingEntity, crit: Boolean) {
        val metadata = MetadataEditor(UndeadWave.instance, entity)
        val arena = Arena.PlayerMap.getNonNull(owner)
        val damage = weapon.calculateDamage(crit) * arena.session.difficulty.playerDamageMultiplier

        metadata.set("enemy.marker.ranged_attacked", true)

        entity.damage(damage, owner)
        entity.velocity = entity.velocity.clone().apply { y = 0.0 }

        metadata.remove("enemy.marker.ranged_attacked")
    }

    private fun playHitEffect(entity: LivingEntity, crit: Boolean) {
        val volume = if (crit) 0.5F else 1.0F
        val pitch = if (crit) 0.5F else 1.0F

        SoundPack(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, pitch, volume).play(owner)
        ParticlePack(Particle.FLAME, 10, 0.5).broadcast(entity.eyeLocation)
    }

    private fun handleAttack(targets: List<LivingEntity>) {
        targets.forEach {
            val crit = weapon.rollCrit()

            damage(it, crit)
            playHitEffect(it, crit)
        }
    }

    private fun limitTargetAmount(targets: List<LivingEntity>, counter: Int): List<LivingEntity> {
        val limit = weapon.maxTargetAmount

        return if (targets.size + counter <= limit) targets
        else randomSublist(targets, limit - counter)
    }
}