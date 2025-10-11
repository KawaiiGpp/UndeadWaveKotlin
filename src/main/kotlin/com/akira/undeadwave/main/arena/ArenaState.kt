package com.akira.undeadwave.main.arena

import net.kyori.adventure.text.format.NamedTextColor

enum class ArenaState(
    val displayName: String,
    val color: NamedTextColor
) {
    FREE("空闲", NamedTextColor.GREEN),
    WORKING("运作中", NamedTextColor.GOLD),
    SHUTDOWN("关闭", NamedTextColor.RED)
}