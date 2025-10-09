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

        runCatching { plugin.configGlobalSettings.loadAll(elements.values) }
            .onFailure { plugin.logError("加载全局配置时发生异常。"); it.printStackTrace() }
    }

    fun saveToConfig() {
        val plugin = UndeadWave.instance

        runCatching { plugin.configGlobalSettings.saveAll(elements.values) }
            .onFailure { plugin.logError("保存全局配置时发生异常。"); it.printStackTrace() }
    }
}