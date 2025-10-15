package com.akira.undeadwave.util

import kotlin.reflect.KProperty

class PropertyDelegate<T: Any> {
    private var raw: T? = null

    operator fun getValue(ref: Any?, property: KProperty<*>): T =
        requireNotNull(raw) { "Property not initialized." }

    operator fun setValue(ref: Any?, property: KProperty<*>, new: T) =
        new.let { raw = it }
}