package com.akira.undeadwave.command

import com.akira.core.api.command.CommandNode
import com.akira.core.api.command.EnhancedExecutor
import com.akira.core.api.command.SenderLimit
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.game.ArenaPreset
import com.akira.undeadwave.game.ArenaPresetCreator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ArenaPresetCommand(plugin: UndeadWave) : EnhancedExecutor(plugin, "arenapreset") {
    init {
        registerNode(New())
        registerNode(Delete())
        registerNode(Create())

        registerNode(SetTotalRounds())
        registerNode(SetEnemyMultiplier())
        registerNode(SetWorld())
        registerNode(SetSpawnpoint())

        registerNode(AddEnemySpawnpoints())
        registerNode(ResetEnemySpawnpoints())
    }

    private inner class New : CommandNode(
        name, SenderLimit.PLAYER, arrayOf("new"), "新建一个地图构建器。"
    ) {
        override fun execute(sender: CommandSender, args: Array<String>): Boolean {
            val player = sender as Player

            if (!ArenaPresetCreator.isRegistered(player.uniqueId)) {
                sender.sendMessage { Component.text("已创建新的地图构建器。", NamedTextColor.GREEN) }

                ArenaPresetCreator.register(player.uniqueId, ArenaPresetCreator())
            } else {
                sender.sendMessage { Component.text("你已经有一个构建器了。", NamedTextColor.RED) }
            }

            return true
        }
    }

    private inner class Delete : Operation(
        arrayOf("delete"), "删除现有的地图构建器。"
    ) {
        override fun operate(player: Player, args: Array<String>) {
            player.sendMessage { Component.text("成功删除现有的构建器。", NamedTextColor.GREEN) }

            ArenaPresetCreator.unregister(player.uniqueId)
        }
    }

    private inner class Create : Operation(
        arrayOf("create", "#名称"), "使用现有的构建器新建地图。"
    ) {
        override fun operate(player: Player, args: Array<String>) {
            val creator = ArenaPresetCreator.getNonNull(player.uniqueId)
            val result = creator.create()

            if (result.success) {
                val name = args[0]

                if (ArenaPreset.isRegistered(name)) {
                    player.sendMessage { Component.text("名为 $name 的地图已经存在了。", NamedTextColor.RED) }
                    return
                }

                ArenaPresetCreator.unregister(player.uniqueId)
                ArenaPreset.register(name, result.product)

                player.sendMessage {
                    Component.text("地图创建成功，命名为 ", NamedTextColor.GREEN)
                        .append(Component.text(name, NamedTextColor.YELLOW))
                        .append(Component.text("。", NamedTextColor.GREEN))
                }

                return
            }

            player.sendMessage { Component.text("地图创建失败。${result.failureMessage}", NamedTextColor.RED) }
        }
    }

    private inner class SetTotalRounds : Configure("total_rounds", "总回合数", true) {
        override fun configure(player: Player, creator: ArenaPresetCreator, valueRaw: String?): String? {
            val value = requireNotNull(valueRaw).toIntOrNull()
                ?: return "值必须是一个整数"

            creator.totalRounds = value
            player.sendMessage { generateSuccessMessage(value) }
            return null
        }
    }

    private inner class SetEnemyMultiplier : Configure("enemy_multiplier", "每回合怪物数量", true) {
        override fun configure(player: Player, creator: ArenaPresetCreator, valueRaw: String?): String? {
            val value = requireNotNull(valueRaw).toIntOrNull()
                ?: return "值必须是一个整数"

            creator.enemyMultiplier = value
            player.sendMessage { generateSuccessMessage(value) }
            return null
        }
    }

    private inner class SetWorld : Configure("world", "世界", false) {
        override fun configure(player: Player, creator: ArenaPresetCreator, valueRaw: String?): String? {
            if (ArenaPreset.isWorldRegistered(player.world))
                return "该世界已被其他地图占用"

            creator.world = player.world
            player.sendMessage { generateSuccessMessage(null) }
            return null
        }
    }

    private inner class SetSpawnpoint : Configure("spawnpoint", "出生点", false) {
        override fun configure(player: Player, creator: ArenaPresetCreator, valueRaw: String?): String? {
            creator.spawnpoint = player.location
            player.sendMessage { generateSuccessMessage(null) }
            return null
        }
    }

    private inner class AddEnemySpawnpoints :
        Configure("enemy_spawnpoints", "怪物刷新点", true, "add", "为现有构建器添加怪物刷新点") {
        override fun configure(player: Player, creator: ArenaPresetCreator, valueRaw: String?): String? {
            val spawnpoints = creator.enemySpawnpoints?.toMutableList() ?: mutableListOf()

            spawnpoints.add(player.location)
            creator.enemySpawnpoints = spawnpoints

            player.sendMessage { generateSuccessMessage("${spawnpoints.size}个") }
            return null
        }
    }

    private inner class ResetEnemySpawnpoints :
        Configure("enemy_spawnpoints", "怪物刷新点", true, "reset", "重置现有构建器的怪物刷新点") {
        override fun configure(player: Player, creator: ArenaPresetCreator, valueRaw: String?): String? {
            if (creator.enemySpawnpoints != null) {
                val spawnpoints = creator.enemySpawnpoints?.toMutableList() ?: mutableListOf()

                spawnpoints.clear()
                creator.enemySpawnpoints = spawnpoints

                player.sendMessage { generateSuccessMessage("空") }
                return null
            } else return "当前没有任何的怪物刷新点。"
        }
    }

    private abstract inner class Operation(
        arguments: Array<String>,
        description: String
    ) : CommandNode(
        name, SenderLimit.PLAYER, arguments, description
    ) {
        final override fun execute(sender: CommandSender, args: Array<String>): Boolean {
            val player = sender as Player

            if (!ArenaPresetCreator.isRegistered(player.uniqueId))
                sender.sendMessage { Component.text("你没有现有的地图构建器。", NamedTextColor.RED) }
            else operate(player, args)

            return true
        }

        abstract fun operate(player: Player, args: Array<String>)
    }

    private abstract inner class Configure(
        field: String,
        private val fieldName: String,
        private val valueRequired: Boolean,
        value: String = "#值",
        description: String = "配置现有构建器的$fieldName。"
    ) : Operation(
        arrayOf("set", field) + if (valueRequired) arrayOf(value) else emptyArray(), description
    ) {
        final override fun operate(player: Player, args: Array<String>) {
            val arg = if (args.size == 1) args[0] else null

            val feedback = configure(player, ArenaPresetCreator.getNonNull(player.uniqueId), arg)
            feedback ?: return

            player.sendMessage { generateFailureMessage(feedback) }
        }

        protected fun generateSuccessMessage(value: Any?): Component {
            var base = Component.text("构建器的 ", NamedTextColor.GREEN)
                .append(Component.text(fieldName, NamedTextColor.YELLOW))
                .append(Component.text(" 已更新", NamedTextColor.GREEN))

            if (value != null) base = base.append(Component.text("为 ", NamedTextColor.GREEN))
                .append(Component.text("$value", NamedTextColor.YELLOW))

            return base.append(Component.text("。", NamedTextColor.GREEN))
        }

        protected fun generateFailureMessage(feedback: String): Component =
            Component.text("无法配置 $fieldName：$feedback。", NamedTextColor.RED)

        abstract fun configure(player: Player, creator: ArenaPresetCreator, valueRaw: String?): String?
    }
}