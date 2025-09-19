package com.akira.undeadwave.game

import com.akira.core.api.util.math.isPositive
import org.bukkit.Location
import org.bukkit.World

class ArenaPresetCreator {
    var totalRounds: Int? = null
    var enemyMultiplier: Int? = null
    var world: World? = null
    var spawnpoint: Location? = null
    var enemySpawnpoints: List<Location>? = null

    fun create(): ArenaPresetCreatorResult {
        fun <T : Any> validate(
            value: T?,
            name: String,
            predicate: (T) -> Pair<Boolean, String> = { true to "" }
        ): String? {
            if (value == null) return "参数 $name 未指定。"
            if (value is Collection<*> && value.isEmpty()) return "列表参数 $name 为空。"

            val (success, reason) = predicate(value)
            if (!success) return "参数 $name 不合规：${reason}。"

            return null
        }

        val failureMessage = validate(totalRounds, "总回合数") { it.isPositive() to "必须大于零" }
            ?: validate(enemyMultiplier, "每回合怪物数量") { it.isPositive() to "必须大于零" }
            ?: validate(world, "地图所在世界")
            ?: validate(spawnpoint, "出生点") { (world == it.world) to "不在同一世界" }
            ?: validate(enemySpawnpoints, "怪物刷新点") { list -> list.all { it.world == world } to "不在同一世界" }

        return failureMessage?.let {
            ArenaPresetCreatorResult.createFailure(it)
        } ?: ArenaPreset(
            requireNotNull(totalRounds),
            requireNotNull(enemyMultiplier),
            requireNotNull(world),
            requireNotNull(spawnpoint),
            requireNotNull(enemySpawnpoints)
        ).let { ArenaPresetCreatorResult.createSuccess(it) }
    }
}