package com.akira.undeadwave.command

import com.akira.core.api.command.CommandNode
import com.akira.core.api.command.EnhancedExecutor
import com.akira.core.api.command.SenderLimit
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.GlobalSettings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GlobalSettingsCommand(plugin: UndeadWave) : EnhancedExecutor(plugin, "globalsettings") {
    init {
        registerNode(Lobby())
    }

    inner class Lobby : CommandNode(
        name, SenderLimit.PLAYER, arrayOf("lobby"), "设置游戏大厅位置。"
    ) {
        override fun execute(sender: CommandSender, args: Array<String>): Boolean {
            val player = sender as Player

            player.sendMessage { Component.text("已设置当前位置为游戏大厅。", NamedTextColor.GREEN) }
            GlobalSettings.lobby.value = player.location

            return true
        }
    }
}