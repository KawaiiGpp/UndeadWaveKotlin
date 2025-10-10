package com.akira.undeadwave.command

import com.akira.core.api.command.CommandNode
import com.akira.core.api.command.EnhancedExecutor
import com.akira.core.api.command.SenderLimit
import com.akira.core.api.util.text.sendLine
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.Global
import com.akira.undeadwave.main.GlobalSettings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GlobalSettingsCommand(plugin: UndeadWave) : EnhancedExecutor(plugin, "globalsettings") {
    init {
        registerNode(Lobby())
        registerNode(Enable())
        registerNode(Disable())
    }

    inner class Disable : CommandNode(
        name, SenderLimit.NONE, arrayOf("disable"), "将游戏设为禁用状态。"
    ) {
        override fun execute(sender: CommandSender, args: Array<String>): Boolean {
            if (Global.enabled) {
                sender.sendMessage { Component.text("已将游戏设为禁用状态。", NamedTextColor.GREEN) }
                Global.enabled = false
            } else {
                sender.sendMessage { Component.text("游戏处于启用状态时才能这么做。", NamedTextColor.RED) }
            }

            return true
        }
    }

    inner class Enable : Operation(
        SenderLimit.NONE, arrayOf("enable"), "尝试自检并启用游戏。"
    ) {
        override fun run(sender: CommandSender, args: Array<String>) {
            val feedback = GlobalSettings.validate()

            if (feedback.failed) {
                sender.sendLine(50, NamedTextColor.DARK_GRAY)
                sender.sendMessage { Component.text("自检未通过，以下是提示信息：", NamedTextColor.RED) }

                feedback.content.forEach {
                    sender.sendMessage {
                        Component.text("✖ ", NamedTextColor.GOLD)
                            .append(Component.text(it, NamedTextColor.GRAY))
                    }
                }
                sender.sendLine(50, NamedTextColor.DARK_GRAY)
            } else {
                Global.enabled = true

                sender.sendMessage { Component.text("自检通过，游戏已启用。", NamedTextColor.GREEN) }
            }
        }
    }

    inner class Lobby : Operation(
        SenderLimit.PLAYER, arrayOf("lobby"), "设置游戏大厅位置。"
    ) {
        override fun run(sender: CommandSender, args: Array<String>) {
            val player = sender as Player

            GlobalSettings.lobby.value = player.location
            player.sendMessage { Component.text("已设置当前位置为游戏大厅。", NamedTextColor.GREEN) }
        }
    }

    abstract inner class Operation(
        senderLimit: SenderLimit,
        args: Array<String>,
        description: String
    ) : CommandNode(
        name, senderLimit, args, description
    ) {
        override fun execute(sender: CommandSender, args: Array<String>): Boolean {
            if (Global.enabled) {
                sender.sendMessage { Component.text("游戏处于禁用状态时才能这么做。", NamedTextColor.RED) }
            } else run(sender, args)

            return true
        }

        abstract fun run(sender: CommandSender, args: Array<String>)
    }
}