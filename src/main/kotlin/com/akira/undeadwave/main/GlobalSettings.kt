package com.akira.undeadwave.main

import com.akira.core.api.util.general.EnhancedPredicate
import com.akira.core.api.util.world.deserializeLocationNullable
import com.akira.core.api.util.world.serializeLocation
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.util.GameSettings

object GlobalSettings : GameSettings() {
    val lobby = createElement(
        "lobby", "大厅",
        { location, section -> section.set("lobby", serializeLocation(location)) },
        { section -> section.getString("lobby")?.let { deserializeLocationNullable(it) } },
        EnhancedPredicate("", "未指定") { true }
    )

    fun loadFromConfig() {
        val plugin = UndeadWave.instance

        elements.forEach { entry ->
            runCatching { plugin.configGlobalSettings.load(entry.value) }
                .onFailure { plugin.logError("无法解析全局配置项：${entry.value.name}") }
        }
    }

    fun saveToConfig() {
        val plugin = UndeadWave.instance

        elements.forEach { entry ->
            runCatching { plugin.configGlobalSettings.save(entry.value) }
                .onFailure { plugin.logError("无法保存全局配置项：${entry.value.name}") }
        }
    }
}