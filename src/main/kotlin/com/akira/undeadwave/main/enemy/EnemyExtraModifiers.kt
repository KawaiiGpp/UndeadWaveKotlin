package com.akira.undeadwave.main.enemy

import com.akira.core.api.config.ConfigSerializable
import com.akira.core.api.util.entity.AttributeEditor
import com.akira.undeadwave.UndeadWave
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity

class EnemyExtraModifiers : ConfigSerializable {
    private val collection = mutableListOf<Element>()
    val elements get() = collection.toList()

    override fun serialize(section: ConfigurationSection) =
        elements.map { "${it.name}:${it.attributeType}:${it.value}:${it.operation}" }
            .let { section["extra_modifiers"] = it }

    override fun deserialize(section: ConfigurationSection) =
        section.getStringList("extra_modifiers")
            .map { Element(it.split(':')) }.forEach(collection::add)

    fun apply(entity: LivingEntity) = elements.forEach { it.apply(entity) }

    class Element(raw: List<String>) {
        val name = raw[0]
        val attributeType = Attribute.valueOf(raw[1])
        val value = raw[2].toDouble()
        val operation = raw[3]

        fun apply(entity: LivingEntity) {
            val editor = AttributeEditor.forEntity(entity, attributeType, UndeadWave.instance.name)

            if (operation == "SET_BASE") editor.base = value
            else editor.add(name, value, AttributeModifier.Operation.valueOf(operation))
        }
    }
}