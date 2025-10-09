package com.akira.undeadwave.util

import com.akira.core.api.util.general.EnhancedPredicate
import com.akira.core.api.util.general.PredicateResult
import org.bukkit.configuration.ConfigurationSection

class SettingElement<T : Any>(
    val name: String,
    val displayName: String,
    private val serializer: (T, ConfigurationSection) -> Unit,
    private val deserializer: (ConfigurationSection) -> T?,
    private val predicate: EnhancedPredicate<T>,
    private var rawValue: T? = null
) {
    var value
        get() = requireNotNull(rawValue) { "Value for preset element $name not initialized." }
        set(value) = value.let { rawValue = value }

    val initialized get() = rawValue != null

    fun validate(): PredicateResult<T> = predicate.test(rawValue)

    fun serialize(section: ConfigurationSection) {
        require(initialized) { "Preset element $name not initialized." }
        serializer(value, section)
    }

    fun deserialize(section: ConfigurationSection) {
        require(!initialized) { "Preset element $name already initialized." }
        requireNotNull(deserializer(section)) { "Failed deserializing $name." }.let { value = it }
    }
}