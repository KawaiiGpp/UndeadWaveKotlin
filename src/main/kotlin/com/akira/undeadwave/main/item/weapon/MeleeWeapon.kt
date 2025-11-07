package com.akira.undeadwave.main.item.weapon

import com.akira.core.api.util.general.rollChance
import com.akira.core.api.util.item.ItemBuilder
import com.akira.core.api.util.item.ItemTagEditor
import com.akira.core.api.util.math.format
import com.akira.core.api.util.math.requiresNonNegative
import com.akira.undeadwave.UndeadWave
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class MeleeWeapon(
    name: String,
    material: Material,
    displayName: String,
    description: List<String>,
    damage: Double,
    critChance: Int,
    critDamage: Int,
    cost: Int,

    val sweeping: Boolean,
    val trueDamage: Boolean,
    val knockbackMultiplier: Double,
    val sweepDamageBonus: Int,
    val lifeStealChance: Int,
    val lifeStealRatio: Double
) : Weapon(
    name, material, displayName, description,
    damage, critChance, critDamage, cost
) {
    val sweepingDamageMultiplier = 0.25
    val lifeStealEnabled get() = lifeStealChance > 0 && lifeStealRatio > 0

    init {
        knockbackMultiplier.requiresNonNegative()
        sweepDamageBonus.requiresNonNegative()
        lifeStealChance.requiresNonNegative()
        lifeStealRatio.requiresNonNegative()
    }

    fun handle(data: MeleeDamageData) {
        val handler = MeleeDamageHandler(this, data)

        data.crit = rollCrit()
        data.trueDamage = trueDamage

        if (handler.handleCooldown()) return
        if (handler.handleAttack()) return

        handler.handleCritParticle()
        handler.handleLifeSteal()
        handler.handleKnockback()
    }

    fun rollLifeSteal() = rollChance(lifeStealChance)

    override fun buildItem(): ItemStack {
        val item = ItemBuilder()
            .apply {
                material = this@MeleeWeapon.material
                displayName = "§6${this@MeleeWeapon.displayName}"
                lore += generateItemLore()
                flags += ItemFlag.entries
            }.build()

        ItemTagEditor.forItemMeta(UndeadWave.instance, item)
            .apply { set("weapon.name", PersistentDataType.STRING, name) }
            .apply(item)

        return item
    }

    private fun generateItemLore(): List<String> =
        mutableListOf<String>().apply {
            add("§f伤害：§c${damage.format()}")

            if (critChance > 0 && critDamage > 0) {
                add("§f暴击伤害：§a+${critDamage}%")
                add("§f暴击率：§a+${critChance}%")
            }

            add("§f总耐久：§b${material.maxDurability}")

            if (knockbackMultiplier != 1.0)
                add("§f击退距离：§e${knockbackMultiplier.format()}x")
            if (sweeping && sweepDamageBonus > 0)
                add("§f横扫加成：§a+${sweepDamageBonus}%")
            add("")

            addAll(generateItemAbilityLore())
            addAll(generateItemLifeStealLore())

            description.forEach { add("§7${it}") }
            add("")

            add("§6近战武器 左键攻击")
        }

    private fun generateItemAbilityLore(): List<String> =
        mutableListOf<String>()
            .apply {
                if (trueDamage) add("§4❂ §c造成真实伤害 §4❂")
                if (sweeping) add("§3⚔ §b造成横扫伤害 §3⚔")
            }
            .apply {
                if (isNotEmpty()) add("")
            }

    private fun generateItemLifeStealLore(): List<String> =
        mutableListOf<String>()
            .apply {
                if (!lifeStealEnabled) return@apply

                add("§5❣ §d生命窃取 §5❣")
                add("§f每次攻击有 §a$lifeStealChance% §f的概率，")
                add("§f窃取到 §6${lifeStealRatio.format()}% §f于")
                add("§f实际攻击伤害的生命值。")
            }
            .apply {
                if (isNotEmpty()) add("")
            }
}