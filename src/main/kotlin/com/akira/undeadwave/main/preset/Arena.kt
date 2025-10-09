package com.akira.undeadwave.main.preset

import com.akira.undeadwave.util.restore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class Arena(val preset: ArenaPreset) {
    val name get() = preset.name
    var state = ArenaState.FREE
        private set

    private var internalSession: ArenaSession? = null
    val session get() = requireNotNull(internalSession) { "Arena session not existing: $name" }

    fun start(sessionInstance: ArenaSession) {
        require(state == ArenaState.FREE) { "Arena already started." }

        state = ArenaState.WORKING
        internalSession = sessionInstance

        session.player.restore()
        session.player.sendMessage { Component.text("游戏开始了！", NamedTextColor.GREEN) }
        session.player.teleport(preset.spawnpoint.value)
    }

    fun end(victory: Boolean) {
        require(state == ArenaState.WORKING) { "Arena not started." }

        internalSession = null
        state = ArenaState.FREE
    }
}