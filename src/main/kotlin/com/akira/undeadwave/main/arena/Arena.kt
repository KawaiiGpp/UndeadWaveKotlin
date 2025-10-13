package com.akira.undeadwave.main.arena

import com.akira.core.api.Manager
import com.akira.undeadwave.main.Global
import com.akira.undeadwave.main.GlobalSettings
import com.akira.undeadwave.util.restore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class Arena(val preset: ArenaPreset, val session: ArenaSession) {
    val name get() = preset.name
    var state = ArenaState.FREE; private set

    fun start() {
        require(Global.enabled) { "Game not enabled." }
        require(state == ArenaState.FREE) { "Arena must be in free state." }

        state = ArenaState.WORKING
        PlayerMap.register(session.player, this)
        PresetMap.register(preset, this)

        session.player.restore()
        session.player.sendMessage { Component.text("游戏开始了！", NamedTextColor.GREEN) }
        session.player.teleport(preset.spawnpoint.value)
    }

    fun shutdown(victory: Boolean) {
        require(state == ArenaState.WORKING) { "Arena is not in working state." }

        session.player.restore()
        session.player.sendMessage { Component.text("游戏结束！胜利状态=$victory。", NamedTextColor.GREEN) }
        session.player.teleport(GlobalSettings.lobby.value)

        PresetMap.unregister(preset)
        PlayerMap.unregister(session.player)
        state = ArenaState.SHUTDOWN
    }

    object PlayerMap : Manager<Player, Arena>()

    object PresetMap : Manager<ArenaPreset, Arena>()
}