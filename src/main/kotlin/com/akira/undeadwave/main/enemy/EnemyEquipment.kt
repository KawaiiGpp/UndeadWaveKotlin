package com.akira.undeadwave.main.enemy

import com.akira.core.api.config.ConfigSerializable
import com.akira.core.api.config.getNonNullSection
import com.akira.core.api.util.item.ItemTagEditor
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.util.ColorMapper
import com.akira.undeadwave.util.PropertyDelegate
import com.akira.undeadwave.util.getOrThrow
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
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
        val config = section.createSection("equipment")

        serializeItem(weapon, "weapon", config)
        serializeItem(helmet, "helmet", config)
        serializeItem(chestplate, "chestplate", config)
        serializeItem(leggings, "leggings", config)
        serializeItem(boots, "boots", config)
    }

    override fun deserialize(section: ConfigurationSection) {
        val config = section.getNonNullSection("equipment")

        weapon = deserializeItem("weapon", config)
        helmet = deserializeItem("helmet", config)
        chestplate = deserializeItem("chestplate", config)
        leggings = deserializeItem("leggings", config)
        boots = deserializeItem("boots", config)
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
            section[name] = "EMPTY"
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
        editor.get("ench", PersistentDataType.STRING)
            ?.takeIf { it != "NONE" }
            ?.let { builder.append(";ench=$it") }

        section[name] = builder.toString()
    }

    private fun deserializeItem(name: String, section: ConfigurationSection): ItemStack {
        val text = requireNotNull(section.getString(name)) { "Cannot get the section for $name." }
        if (text == "EMPTY") return ItemStack(Material.AIR)

        val rawMeta = text.split(';')
        val material = Material.valueOf(rawMeta[0])

        require(material.isItem) { "Material for equipment must be an Item: $material" }

        val item = ItemStack(material)
        val meta = item.itemMeta
        val editor = ItemTagEditor(UndeadWave.instance, meta)

        fun find(name: String, param: Boolean = false): String? =
            rawMeta.firstOrNull {
                if (param) it.startsWith("$name=")
                else it == name
            }?.let {
                return if (!param) it
                else it.split('=')[1]
            }

        editor.set(
            "shiny", PersistentDataType.BOOLEAN,

            find("shiny", false)?.also {
                meta.addEnchant(Enchantment.DURABILITY, 1, true)
            } != null
        )

        editor.set(
            "ench", PersistentDataType.STRING,

            find("ench", true)?.also {
                it.split(',').forEach { raw ->
                    val split = raw.split(':')
                    val enchantment = Registry.ENCHANTMENT.getOrThrow(NamespacedKey.minecraft(split[0]))
                    val level = split[1].toInt()

                    meta.addEnchant(enchantment, level, true)
                }
            } ?: "NONE"
        )

        if (meta is LeatherArmorMeta) {
            editor.set(
                "color", PersistentDataType.STRING,

                find("color", true)?.also {
                    val color = ColorMapper.get(it)
                        ?: it.split(',').run {
                            Color.fromRGB(
                                this[0].toInt(),
                                this[1].toInt(),
                                this[2].toInt()
                            )
                        }

                    meta.setColor(color)
                } ?: "NONE"
            )
        }

        editor.apply(item)
        return item
    }
}