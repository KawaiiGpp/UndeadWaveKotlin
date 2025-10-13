package com.akira.undeadwave.command

import com.akira.core.api.command.CommandNode
import com.akira.core.api.command.EnhancedExecutor
import com.akira.core.api.command.SenderLimit
import com.akira.core.api.util.text.sendLine
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.Global
import com.akira.undeadwave.main.arena.Arena
import com.akira.undeadwave.main.arena.ArenaDifficulty
import com.akira.undeadwave.main.arena.ArenaPreset
import com.akira.undeadwave.main.arena.ArenaSession
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UndeadWaveCommand(plugin: UndeadWave) : EnhancedExecutor(plugin, "undeadwave") {
    init {
        registerNode(Status())
        registerNode(Quit())

        ArenaDifficulty.entries.forEach { registerNode(Join(it)) }
    }

    inner class Join(private val difficulty: ArenaDifficulty) : Operation(
        arrayOf("join", "#地图名称", difficulty.identifier), "以${difficulty.displayName}难度开始游戏。"
    ) {
        override fun run(player: Player, args: Array<String>) {
            val name = args[0]
            val preset = ArenaPreset.get(name)

            if (preset == null) {
                player.sendMessage { Component.text("找不到对应名称的地图。", NamedTextColor.RED) }
                return
            }

            if (Arena.PlayerMap.isRegistered(player)) {
                player.sendMessage { Component.text("你已经在游玩这张地图了。", NamedTextColor.RED) }
                return
            }

            if (Arena.PresetMap.isRegistered(preset)) {
                player.sendMessage { Component.text("该地图并未处于空闲状态。", NamedTextColor.RED) }
                return
            }

            val session = ArenaSession(player, difficulty)
            val arena = Arena(preset, session)

            player.sendMessage {
                Component.text("你已加入 ", NamedTextColor.GREEN)
                    .append(Component.text(preset.displayName.value, NamedTextColor.YELLOW))
                    .append(Component.text("，难度设为 ", NamedTextColor.GREEN))
                    .append(Component.text(difficulty.displayName, difficulty.color))
                    .append(Component.text("。", NamedTextColor.GREEN))
            }

            arena.start()
        }
    }

    inner class Quit : Operation(
        arrayOf("quit"), "退出当前游戏，成绩判负。"
    ) {
        override fun run(player: Player, args: Array<String>) {
            val arena = Arena.PlayerMap.get(player)

            if (arena == null) {
                player.sendMessage { Component.text("你不在任何的游戏地图中。", NamedTextColor.RED) }
                return
            }

            arena.shutdown(false)
            player.sendMessage { Component.text("你已退出当前游戏。", NamedTextColor.GREEN) }
        }
    }

    inner class Status : CommandNode(
        name, SenderLimit.NONE, arrayOf("status"), "查看当前所有地图状态。"
    ) {
        override fun execute(sender: CommandSender, args: Array<String>): Boolean {
            val presets = ArenaPreset.container.values

            if (Global.disabled || presets.isEmpty()) {
                sender.sendMessage { Component.text("目前没有任何可用的游戏地图。", NamedTextColor.RED) }
                return true
            }

            sender.sendLine(75, NamedTextColor.DARK_GRAY)
            sender.sendMessage {
                Component.text("目前共有 ", NamedTextColor.WHITE)
                    .append(Component.text(presets.size.toString(), NamedTextColor.YELLOW))
                    .append(Component.text(" 张可用的游戏地图。", NamedTextColor.WHITE))
            }

            presets.forEach {
                val arena = Arena.PresetMap.get(it)

                if (arena != null) {
                    val component = text(it, "占线", NamedTextColor.RED)
                        .append(Component.text(" -", NamedTextColor.DARK_GRAY))
                        .append(Component.text(" ${arena.session.difficulty.displayName}模式", NamedTextColor.GRAY))
                        .append(Component.text(" ${arena.session.player.name}", NamedTextColor.GRAY))

                    sender.sendMessage { component }
                } else {
                    sender.sendMessage { text(it, "空闲", NamedTextColor.DARK_GREEN) }
                }
            }

            sender.sendLine(75, NamedTextColor.DARK_GRAY)
            return true
        }

        private fun text(preset: ArenaPreset, text: String, color: NamedTextColor): TextComponent =
            Component.text("■ $text ", color)
                .append(Component.text("- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("${preset.displayName.value} ${preset.name}", NamedTextColor.GRAY))
    }

    abstract inner class Operation(
        args: Array<String>,
        description: String,
    ) : CommandNode(
        name, SenderLimit.PLAYER, args, description
    ) {
        final override fun execute(sender: CommandSender, args: Array<String>): Boolean {
            if (Global.disabled) {
                sender.sendMessage { Component.text("目前游戏已被管理员设为禁用状态。", NamedTextColor.RED) }
            } else run(sender as Player, args)

            return true
        }

        abstract fun run(player: Player, args: Array<String>)
    }
}