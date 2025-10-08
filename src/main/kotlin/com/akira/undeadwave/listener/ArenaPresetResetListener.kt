package com.akira.undeadwave.listener

import com.akira.undeadwave.main.preset.ArenaPreset
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class ArenaPresetResetListener : Listener {
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val uniqueId = event.player.uniqueId

        if (!ArenaPreset.Creator.isRegistered(uniqueId)) return
        ArenaPreset.Creator.unregister(uniqueId)
    }
}