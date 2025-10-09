package com.akira.undeadwave.main.preset

import net.kyori.adventure.text.format.NamedTextColor

enum class ArenaDifficulty(
    val displayName: String,
    val color: NamedTextColor,
    val playerDamageMultiplier: Double,
    val enemyDamageMultiplier: Double,
    val enemySpeedMultiplier: Double,
    val scoreMultiplier: Double
) {
    EASY("简单", NamedTextColor.DARK_GREEN, 1.5, 0.75, 0.8, 0.5),
    NORMAL("普通", NamedTextColor.GOLD, 1.0, 1.0, 1.0, 1.0),
    HARD("困难", NamedTextColor.DARK_RED, 0.75, 1.5, 1.25, 2.0)
}