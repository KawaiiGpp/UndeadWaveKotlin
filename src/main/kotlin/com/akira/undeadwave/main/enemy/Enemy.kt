package com.akira.undeadwave.main.enemy

import com.akira.core.api.EnhancedManager
import com.akira.core.api.config.ConfigSerializable
import com.akira.core.api.config.getNonNullSection
import com.akira.core.api.util.entity.AttributeEditor
import com.akira.core.api.util.entity.getFinalMaxHealth
import com.akira.core.api.util.entity.getNonNullAttribute
import com.akira.core.api.util.entity.setBaseMaxHealth
import com.akira.core.api.util.world.worldNonNull
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.util.PropertyDelegate
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Ageable
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class Enemy(val name: String) : ConfigSerializable {
    var displayName by PropertyDelegate<String>(); private set
    var entityType by PropertyDelegate<EntityType>(); private set
    var health by PropertyDelegate<Double>(); private set
    var damage by PropertyDelegate<Double>(); private set
    var speedBonus by PropertyDelegate<Int>(); private set
    var availableRoundFrom by PropertyDelegate<Int>(); private set
    var availableRoundTo by PropertyDelegate<Int>(); private set
    var weight by PropertyDelegate<Int>(); private set
    var reward by PropertyDelegate<Int>(); private set
    var equipment by PropertyDelegate<EnemyEquipment>(); private set

    override fun serialize(section: ConfigurationSection) {
        section["type"] = entityType.name
        section["display_name"] = displayName
        section["health"] = health
        section["damage"] = damage
        section["speed_bonus"] = speedBonus
        section["available_round.from"] = availableRoundFrom
        section["available_round.to"] = availableRoundTo
        section["weight"] = weight
        section["reward"] = reward

        equipment.serialize(section.createSection("equipment"))
    }

    override fun deserialize(section: ConfigurationSection) {
        fun <T : Any> nonNull(key: String, apply: ConfigurationSection.(String) -> T?): T =
            requireNotNull(section.apply(key)) { "Failed parsing the key $key" }

        entityType = EntityType.valueOf(nonNull("type", ConfigurationSection::getString))
        displayName = nonNull("display_name", ConfigurationSection::getString)
        health = nonNull("health", ConfigurationSection::getDouble)
        damage = nonNull("damage", ConfigurationSection::getDouble)
        speedBonus = nonNull("speed_bonus", ConfigurationSection::getInt)
        availableRoundFrom = nonNull("available_round.from", ConfigurationSection::getInt)
        availableRoundTo = nonNull("available_round.to", ConfigurationSection::getInt)
        weight = nonNull("weight", ConfigurationSection::getInt)
        reward = nonNull("reward", ConfigurationSection::getInt)

        equipment = EnemyEquipment().apply { deserialize(section.getNonNullSection("equipment")) }
    }

    fun spawn(location: Location): LivingEntity {
        val entity = location.worldNonNull.spawnEntity(location, entityType)
        require(entity is LivingEntity) { "Enemy must be a Living Entity." }

        fun modify(name: String, type: Attribute, value: Double, operation: Operation) =
            AttributeEditor(entity.getNonNullAttribute(type), UndeadWave.instance.name)
                .add("ingame.enemy.modifiers.$name", value, operation)

        entity.isPersistent = true
        entity.removeWhenFarAway = false
        entity.maximumNoDamageTicks = 0
        entity.canPickupItems = false

        (entity as? Ageable)?.setAdult()
        entity.vehicle?.remove()

        entity.setBaseMaxHealth(health)
        entity.health = entity.getFinalMaxHealth()
        equipment.apply(entity)

        modify("speed_bonus", Attribute.GENERIC_MOVEMENT_SPEED, speedBonus / 100.0, Operation.ADD_SCALAR)
        return entity
    }

    fun canSpawn(round: Int): Boolean = round >= availableRoundFrom && round <= availableRoundTo

    companion object : EnhancedManager<Enemy>() {
        override fun transform(element: Enemy): String = element.name

        fun loadFromConfig() {
            val plugin = UndeadWave.instance

            runCatching { plugin.configEnemy.loadAll().forEach(this::register) }
                .onSuccess {
                    if (container.isEmpty()) plugin.logInfo("未从配置中读取到任何怪物种类。")
                    else plugin.logInfo("已从配置中加载 ${container.size} 种怪物种类。")
                }
                .onFailure {
                    plugin.logError("加载配置中现有怪物种类时发生异常。")
                    it.printStackTrace()
                }
        }

        fun saveToConfig() {
            val plugin = UndeadWave.instance

            runCatching { plugin.configEnemy.saveAll(container.values) }
                .onSuccess {
                    if (container.isNotEmpty())
                        plugin.logInfo("已保存 ${container.size} 种怪物至配置文件。")
                }
                .onFailure {
                    plugin.logError("保存现有怪物种类到配置文件时发生异常。")
                    it.printStackTrace()
                }
        }
    }
}