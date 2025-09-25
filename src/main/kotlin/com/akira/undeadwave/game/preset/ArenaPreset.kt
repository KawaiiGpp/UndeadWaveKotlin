package com.akira.undeadwave.game.preset

class ArenaPreset {
    private val map = mutableMapOf<ArenaPresetType<*>, Any>()
    val container get() = map.toMap()

    inline fun <reified T : Any> get(type: ArenaPresetType<T>): T? = container[type] as? T

    fun <T : Any> set(type: ArenaPresetType<T>, value: T) = value.let { map[type] = it }
}