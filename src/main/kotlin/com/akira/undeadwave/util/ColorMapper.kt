package com.akira.undeadwave.util

import com.akira.core.api.Manager
import org.bukkit.Color

object ColorMapper : Manager<String, Color>() {
    init {
        Color::class.java.fields.forEach {
            if (it.type != Color::class.java) return@forEach

            val color = it.get(null) as? Color ?: return@forEach
            this.register(it.name, color)
        }
    }

    fun getColorName(color: Color): String? =
        container.filter { it.value == color }.map { it.key }.firstOrNull()
}