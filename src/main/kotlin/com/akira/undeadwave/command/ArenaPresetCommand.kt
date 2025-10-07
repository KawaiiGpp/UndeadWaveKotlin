package com.akira.undeadwave.command

import com.akira.core.api.command.CommandNode
import com.akira.core.api.command.EnhancedExecutor
import com.akira.core.api.command.SenderLimit
import com.akira.core.api.util.text.sendLine
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.preset.ArenaPreset
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ArenaPresetCommand(plugin: UndeadWave) : EnhancedExecutor(plugin, "arenapreset") {
    init {
        registerNode(New())
        registerNode(Delete())
        registerNode(Create())

        registerNode(ConfigureValue(ArenaPreset::totalRounds, String::toIntOrNull))
        registerNode(ConfigureValue(ArenaPreset::enemyMultiplier, String::toIntOrNull))
        registerNode(ConfigureValue(ArenaPreset::displayName) { it })

        registerNode(Configure(ArenaPreset::enemySpawnpoints) { e, p ->
            val list = if (e.initialized) e.value.toMutableList() else mutableListOf()
            list.apply { this.add(p.location) }.let { e.value = it }
        })

        registerNode(Configure(ArenaPreset::world) { e, p -> e.value = p.world })
        registerNode(Configure(ArenaPreset::spawnpoint) { e, p -> e.value = p.location })
    }

    inner class New : CommandNode(
        name, SenderLimit.PLAYER,
        arrayOf("new", "#内部名称"),
        "新建一个空白预设模板。"
    ) {
        override fun execute(sender: CommandSender, args: Array<String>): Boolean {
            val name = args[0]
            val uniqueId = (sender as Player).uniqueId

            if (ArenaPreset.Creator.isRegistered(uniqueId)) {
                sender.sendMessage { Component.text("你已创建了一个预设模板。", NamedTextColor.RED) }
                return true
            }

            if (ArenaPreset.isRegistered(name)) {
                sender.sendMessage { Component.text("相同内部名的预设已经存在。", NamedTextColor.RED) }
                return true
            }

            sender.sendMessage {
                Component.text("已创建空白预设模板，内部名为 ", NamedTextColor.GREEN)
                    .append(Component.text(name, NamedTextColor.YELLOW))
                    .append(Component.text("。", NamedTextColor.GREEN))
            }

            ArenaPreset.Creator.register(uniqueId, ArenaPreset(name))
            return true
        }
    }

    inner class Delete : Operation(
        arrayOf("delete"),
        "删除现有的预设模板。"
    ) {
        override fun run(player: Player, preset: ArenaPreset, args: Array<String>) {
            player.sendMessage {
                Component.text("已删除现有的预设模板，内部名为 ", NamedTextColor.GREEN)
                    .append(Component.text(preset.name, NamedTextColor.YELLOW))
                    .append(Component.text("。", NamedTextColor.GREEN))
            }

            ArenaPreset.Creator.unregister(player.uniqueId)
        }
    }

    inner class Create : Operation(
        arrayOf("create"),
        "以现有预设模板创建新的预设。"
    ) {
        override fun run(player: Player, preset: ArenaPreset, args: Array<String>) {
            val feedback = preset.validate()

            if (feedback.passed) {
                player.sendMessage {
                    Component.text("已成功应用预设模板，内部名为 ", NamedTextColor.GREEN)
                        .append(Component.text(preset.name, NamedTextColor.YELLOW))
                        .append(Component.text("。", NamedTextColor.GREEN))
                }

                ArenaPreset.register(preset)
                ArenaPreset.Creator.unregister(player.uniqueId)
                return
            }

            player.sendLine(50, NamedTextColor.DARK_GRAY)
            player.sendMessage { Component.text("预设模板未通过自检，参考信息：", NamedTextColor.RED) }

            feedback.content.forEach {
                player.sendMessage {
                    Component.text("✖ ", NamedTextColor.GOLD)
                        .append(Component.text(it, NamedTextColor.GRAY))
                }
            }
            player.sendLine(50, NamedTextColor.DARK_GRAY)
        }
    }

    inner class ConfigureValue<T : Any>(
        private val supplier: (ArenaPreset) -> ArenaPreset.Element<T>,
        private val parser: (String) -> T?
    ) : Operation(
        supplier(ArenaPreset.internal).let { arrayOf("set", it.name, "#${it.displayName}") },
        supplier(ArenaPreset.internal).displayName.let { "设定现有预设模板的$it。" }
    ) {
        override fun run(player: Player, preset: ArenaPreset, args: Array<String>) {
            val result = parser(args[0])

            if (result == null) {
                player.sendMessage { Component.text("无法解析输入的参数：${args[0]}。", NamedTextColor.RED) }
                return
            }

            val element = supplier(preset)

            player.sendMessage {
                Component.text("预设模板的 ", NamedTextColor.WHITE)
                    .append(Component.text(element.displayName, NamedTextColor.YELLOW))
                    .append(Component.text(" 已被更新为 ", NamedTextColor.WHITE))
                    .append(Component.text(result.toString(), NamedTextColor.GREEN))
                    .append(Component.text("。", NamedTextColor.WHITE))
            }

            element.value = result
        }
    }

    inner class Configure<T : Any>(
        private val supplier: (ArenaPreset) -> ArenaPreset.Element<T>,
        private val applier: (ArenaPreset.Element<T>, Player) -> Unit,
    ) : Operation(
        arrayOf("set", supplier(ArenaPreset.internal).name),
        supplier(ArenaPreset.internal).displayName.let { "配置现有预设模板的$it。" }
    ) {
        override fun run(player: Player, preset: ArenaPreset, args: Array<String>) {
            val element = supplier(preset)

            player.sendMessage {
                Component.text("预设模板的 ", NamedTextColor.WHITE)
                    .append(Component.text(element.displayName, NamedTextColor.YELLOW))
                    .append(Component.text(" 已被更新。", NamedTextColor.WHITE))
            }

            applier(element, player)
        }
    }

    abstract inner class Operation(
        args: Array<String>, description: String
    ) : CommandNode(
        name, SenderLimit.PLAYER, args, description
    ) {
        final override fun execute(sender: CommandSender, args: Array<String>): Boolean {
            val player = sender as Player
            val uniqueId = player.uniqueId
            val preset = ArenaPreset.Creator.get(uniqueId)

            if (preset == null) {
                sender.sendMessage { Component.text("你没有现有的预设模板。", NamedTextColor.RED) }
                return true
            }

            this.run(player, preset, args)
            return true
        }

        abstract fun run(player: Player, preset: ArenaPreset, args: Array<String>)
    }
}