package com.akira.undeadwave.main.arena

import com.akira.core.api.EnhancedManager
import com.akira.core.api.Manager
import com.akira.core.api.util.general.EnhancedPredicate
import com.akira.core.api.util.general.PredicateResult
import com.akira.core.api.util.general.ValidateFeedback
import com.akira.core.api.util.world.deserializeLocation
import com.akira.core.api.util.world.deserializeLocationNullable
import com.akira.core.api.util.world.serializeLocation
import com.akira.core.api.util.world.worldNonNull
import com.akira.undeadwave.UndeadWave
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import java.util.*

class ArenaPreset(val name: String) {
    private val map = mutableMapOf<String, Element<*>>()
    val elements get() = map.toMap()

    val displayName = createElement(
        "display_name", "名称",
        { string, section -> section.set("display_name", string) },
        { section -> section.getString("display_name") },
        EnhancedPredicate("不可为空白", "未指定") { it.isNotBlank() })

    val totalRounds = createElement(
        "total_rounds", "总回合数",
        { int, section -> section.set("total_rounds", int) },
        { section -> section.getInt("total_rounds") },
        EnhancedPredicate("需为正整数", "未指定") { it > 0 })

    val enemyMultiplier = createElement(
        "enemy_multiplier", "刷怪量",
        { int, section -> section.set("enemy_multiplier", int) },
        { section -> section.getInt("enemy_multiplier") },
        EnhancedPredicate("需为正整数", "未指定") { it > 0 })

    val world = createElement(
        "world", "世界",
        { world, section -> section.set("world", world.name) },
        { section -> section.getString("world")?.let { Bukkit.getWorld(it) } },
        EnhancedPredicate("", "未指定") { true })

    val spawnpoint = createElement(
        "spawnpoint", "出生点",
        { location, section -> section.set("spawnpoint", serializeLocation(location)) },
        { section -> section.getString("spawnpoint")?.let { deserializeLocationNullable(it) } },
        EnhancedPredicate("", "未指定") { true })

    val enemySpawnpoints = createElement(
        "enemy_spawnpoints", "怪物刷新点",
        { locations, section -> section.set("enemy_spawnpoints", locations.map { serializeLocation(it) }) },
        { section -> section.getStringList("enemy_spawnpoints").map { deserializeLocation(it) } },
        EnhancedPredicate("不可为空", "未指定") { it.isNotEmpty() })

    fun validate(): ValidateFeedback {
        val feedback = ValidateFeedback()

        elements.values.map { it to it.validate() }.filter { it.second.failure }
            .forEach { feedback.add("参数 ${it.first.displayName} 异常：${it.second.failureMessage}。") }

        if (feedback.passed) {
            validateWorld(spawnpoint.displayName, spawnpoint.value)?.let(feedback::add)
            enemySpawnpoints.value.forEach { validateWorld(enemySpawnpoints.displayName, it)?.let(feedback::add) }
        }

        return feedback
    }

    private fun <T : Any> createElement(
        name: String,
        displayName: String,
        serializer: (T, ConfigurationSection) -> Unit,
        deserializer: (ConfigurationSection) -> T?,
        predicate: EnhancedPredicate<T>,
        rawValue: T? = null
    ): Element<T> = Element(
        name, displayName, serializer,
        deserializer, predicate, rawValue
    ).also { registerElement(it) }

    private fun registerElement(element: Element<*>) {
        val name = element.name

        require(!elements.containsKey(name)) { "Preset element $name already existing." }
        map[name] = element
    }

    private fun validateWorld(name: String, location: Location): String? =
        if (location.worldNonNull == world.value) null
        else "位置 $name 所在世界不一致。"

    class Element<T : Any>(
        val name: String,
        val displayName: String,
        private val serializer: (T, ConfigurationSection) -> Unit,
        private val deserializer: (ConfigurationSection) -> T?,
        private val predicate: EnhancedPredicate<T>,
        private var rawValue: T? = null
    ) {
        var value
            get() = requireNotNull(rawValue) { "Value for preset element $name not initialized." }
            set(value) = value.let { rawValue = value }

        val initialized get() = rawValue != null

        fun validate(): PredicateResult<T> = predicate.test(rawValue)

        fun serialize(section: ConfigurationSection) {
            require(initialized) { "Preset element $name not initialized." }
            serializer(value, section)
        }

        fun deserialize(section: ConfigurationSection) {
            require(!initialized) { "Preset element $name already initialized." }
            requireNotNull(deserializer(section)) { "Failed deserializing $name." }.let { value = it }
        }
    }

    object Creator : Manager<UUID, ArenaPreset>()

    companion object : EnhancedManager<ArenaPreset>() {
        val internal = ArenaPreset("_internal_")

        override fun transform(element: ArenaPreset): String = element.name

        fun loadFromConfig() {
            val plugin = UndeadWave.instance

            runCatching { plugin.configArenaPreset.loadAll().forEach(this::register) }
                .onSuccess {
                    if (container.isNotEmpty())
                        plugin.logInfo("已从配置文件加载 ${container.size} 个地图预设。")
                }
                .onFailure {
                    plugin.logError("从配置文件加载地图预设时发生异常。")
                    it.printStackTrace()
                }
        }

        fun saveToConfig() {
            val plugin = UndeadWave.instance

            runCatching { plugin.configArenaPreset.saveAll(container.values) }
                .onSuccess {
                    if (container.isNotEmpty())
                        plugin.logInfo("已保存 ${container.size} 个地图预设至配置文件。")
                }
                .onFailure {
                    plugin.logError("保存地图预设至配置文件时发生异常。")
                    it.printStackTrace()
                }
        }
    }
}