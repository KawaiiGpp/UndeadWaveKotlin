package com.akira.undeadwave.main.item.weapon

import com.akira.core.api.util.general.rollChance
import com.akira.core.api.util.item.ItemTagEditor
import com.akira.undeadwave.UndeadWave
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class Weapon(
    val name: String,
    val material: Material,
    val displayName: String,
    val description: List<String>,
    val damage: Double,
    val critChance: Int,
    val critDamage: Int,
    val cost: Int
) {
    fun rollCrit(): Boolean = rollChance(critChance)

    fun calculateDamage(crit: Boolean): Double =
        if (!crit) damage else damage * (1 + (critDamage / 100.0))

    fun matches(item: ItemStack): Boolean =
        ItemTagEditor.forItemMeta(UndeadWave.instance, item)
            .get("weapon.name", PersistentDataType.STRING) == name

    abstract fun buildItem(): ItemStack
}