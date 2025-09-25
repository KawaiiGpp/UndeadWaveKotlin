package com.akira.undeadwave

import com.akira.core.api.AkiraPlugin

class UndeadWave : AkiraPlugin() {
    companion object {
        lateinit var instance: UndeadWave
            private set
    }

    init {
        instance = this
    }
}