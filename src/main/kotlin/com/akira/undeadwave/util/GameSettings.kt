package com.akira.undeadwave.util

import com.akira.core.api.util.general.EnhancedPredicate
import com.akira.core.api.util.general.ValidateFeedback
import org.bukkit.configuration.ConfigurationSection

abstract class GameSettings {
    private val map = mutableMapOf<String, SettingElement<*>>()
    val elements get() = map.toMap()

    fun validate(): ValidateFeedback {
        val feedback = ValidateFeedback()

        elements.values
            .map { it to it.validate() }
            .filter { it.second.failure }
            .forEach { feedback.add("参数 ${it.first.displayName} 异常：${it.second.failureMessage}。") }

        validateExtra(feedback)
        return feedback
    }

    protected fun <T : Any> createElement(
        name: String,
        displayName: String,
        serializer: (T, ConfigurationSection) -> Unit,
        deserializer: (ConfigurationSection) -> T?,
        predicate: EnhancedPredicate<T>,
        rawValue: T? = null
    ): SettingElement<T> = SettingElement(
        name, displayName, serializer,
        deserializer, predicate, rawValue
    ).also { registerElement(it) }

    protected open fun validateExtra(feedback: ValidateFeedback) {}

    private fun registerElement(element: SettingElement<*>) {
        val name = element.name

        require(!elements.containsKey(name)) { "Preset element $name already existing." }
        map[name] = element
    }
}