package com.akira.undeadwave.main.enemy

import com.akira.core.api.config.ConfigSerializable
import com.akira.core.api.util.item.ItemTagEditor
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.util.ColorMapper
import com.akira.undeadwave.util.PropertyDelegate
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.persistence.PersistentDataType

class EnemyEquipment : ConfigSerializable {
    var weapon by PropertyDelegate<ItemStack>(); private set
    var helmet by PropertyDelegate<ItemStack>(); private set
    var chestplate by PropertyDelegate<ItemStack>(); private set
    var leggings by PropertyDelegate<ItemStack>(); private set
    var boots by PropertyDelegate<ItemStack>(); private set

    override fun serialize(section: ConfigurationSection) {
        serializeItem(weapon, "weapon", section)
        serializeItem(helmet, "helmet", section)
        serializeItem(chestplate, "chestplate", section)
        serializeItem(leggings, "leggings", section)
        serializeItem(boots, "boots", section)
    }

    override fun deserialize(section: ConfigurationSection) {
        weapon = deserializeItem("weapon", section)
        helmet = deserializeItem("helmet", section)
        chestplate = deserializeItem("chestplate", section)
        leggings = deserializeItem("leggings", section)
        boots = deserializeItem("boots", section)
    }

    fun apply(entity: LivingEntity) {
        val equipment = entity.equipment
        requireNotNull(equipment) { "Entity ${entity.type} doesn't have an equipment." }

        equipment.setItemInMainHand(weapon)
        equipment.helmet = helmet
        equipment.chestplate = chestplate
        equipment.leggings = leggings
        equipment.boots = boots
    }

    private fun serializeItem(item: ItemStack, name: String, section: ConfigurationSection) {
        if (item.type == Material.AIR) {
            section[name] = null
            return
        }

        val builder = StringBuilder(item.type.name)
        val editor = ItemTagEditor(UndeadWave.instance, item.itemMeta)

        editor.get("shiny", PersistentDataType.BOOLEAN)
            ?.takeIf { it }
            ?.let { builder.append(";shiny") }
        editor.get("color", PersistentDataType.STRING)
            ?.takeIf { it != "NONE" }
            ?.let { builder.append(";color=$it") }

        section[name] = builder.toString()
    }

    private fun deserializeItem(name: String, section: ConfigurationSection): ItemStack {
        val rawMeta = section.getString(name)?.split(';') ?: return ItemStack(Material.AIR)
        val material = Material.valueOf(rawMeta[0])

        require(material.isItem) { "Material for equipment must be an Item: $material" }

        val item = ItemStack(material)
        val meta = item.itemMeta
        val editor = ItemTagEditor(UndeadWave.instance, meta)

        if (rawMeta.contains("shiny")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true)
            editor.set("shiny", PersistentDataType.BOOLEAN, true)
        } else {
            editor.set("shiny", PersistentDataType.BOOLEAN, false)
        }

        if (meta is LeatherArmorMeta) {
            rawMeta.firstOrNull { it.startsWith("color=") }?.let {
                val text = it.substring(6)
                val color = ColorMapper.get(text) ?: text.split(',').run {
                    Color.fromRGB(this[0].toInt(), this[1].toInt(), this[2].toInt())
                }

                meta.setColor(color)
                editor.set("color", PersistentDataType.STRING, text)
            } ?: editor.set("color", PersistentDataType.STRING, "NONE")
        }

        editor.apply(item)
        return item
    }
}