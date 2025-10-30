package com.akira.undeadwave.main.item.weapon

import com.akira.core.api.util.general.Cooldown
import com.akira.core.api.util.item.ItemBuilder
import com.akira.core.api.util.item.ItemTagEditor
import com.akira.core.api.util.math.format
import com.akira.core.api.util.world.ParticlePack
import com.akira.core.api.util.world.SoundPack
import com.akira.undeadwave.UndeadWave
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class RangedWeapon(
    name: String,
    material: Material,
    displayName: String,
    description: List<String>,
    damage: Double,
    critChance: Int,
    critDamage: Int,
    cost: Int,

    val cooldownTicks: Long,
    val sound: SoundPack,
    val particle: ParticlePack,
    val distance: Int,
    val maxTargetAmount: Int,
    val stepLength: Double,
    val totalDurability: Int,
    val hitboxMultiplier: Double,
    val piercing: Boolean,
    val repeatHit: Boolean
) : Weapon(
    name, material, displayName, description,
    damage, critChance, critDamage, cost
) {
    val cooldownHandler = Cooldown(UndeadWave.instance)

    fun shoot(player: Player) {
        if (cooldownHandler.inCooldown) return

        sound.play(player)
        RangedWeaponBullet(player, this).launch()
        if (consumeDurability(player)) return

        cooldownHandler.start(cooldownTicks)
    }

    override fun buildItem(): ItemStack {
        val item = ItemBuilder()
            .apply {
                material = this@RangedWeapon.material
                displayName = "§b${this@RangedWeapon.displayName}"
                lore += generateItemLore()
                flags += ItemFlag.entries
            }.build()

        ItemTagEditor.forItemMeta(UndeadWave.instance, item)
            .apply {
                set("weapon.name", PersistentDataType.STRING, name)
                set("weapon.durability.total", PersistentDataType.INTEGER, totalDurability)
                set("weapon.durability.damage", PersistentDataType.INTEGER, 0)
            }.apply(item)

        return item
    }

    override fun matches(item: ItemStack): Boolean =
        ItemTagEditor.forItemMeta(UndeadWave.instance, item)
            .get("weapon.name", PersistentDataType.STRING) == name

    private fun generateItemLore(): List<String> =
        mutableListOf<String>().apply {
            add("§f伤害：§c${damage.format()}")

            if (critChance > 0 && critDamage > 0) {
                add("§f暴击伤害：§a+${critDamage}%")
                add("§f暴击率：§a+${critChance}%")
            }

            add("§f射击距离：§e${distance}m")
            add("§f冷却：§e${(cooldownTicks / 20.0).format()}s")

            if (maxTargetAmount > 1)
                add("§f最大命中数：§d${maxTargetAmount}")

            add("§f总耐久：§b${totalDurability}")
            add("")

            description.forEach { add("§7${it}") }
            add("")

            if (piercing) {
                if (repeatHit)
                    add("§4⚔ §c造成增强穿透伤害 §4⚔")
                else
                    add("§6❁ §e造成穿透伤害 §6❁")

                add("")
            }

            add("§b远程武器 右键发射")
        }

    private fun consumeDurability(player: Player): Boolean {
        val item = player.inventory.itemInMainHand
        val tag = ItemTagEditor.forItemMeta(UndeadWave.instance, item)

        val damage = tag.getNonNull("weapon.durability.damage", PersistentDataType.INTEGER) + 1
        val total = tag.getNonNull("weapon.durability.total", PersistentDataType.INTEGER)

        if (damage <= total) {
            tag.set("weapon.durability.damage", PersistentDataType.INTEGER, damage)
            tag.apply(item)
            return false
        } else {
            SoundPack(Sound.BLOCK_GLASS_BREAK).play(player)
            player.inventory.setItemInMainHand(null)
            return true
        }
    }
}