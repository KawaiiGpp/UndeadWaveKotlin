package com.akira.undeadwave.listener

import com.akira.undeadwave.main.arena.Arena
import com.akira.undeadwave.main.arena.ArenaPreset
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class GeneralListener : Listener {
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uniqueId = player.uniqueId

        Arena.PlayerMap.get(player)?.shutdown(false)

        if (!ArenaPreset.Creator.isRegistered(uniqueId)) return
        ArenaPreset.Creator.unregister(uniqueId)
    }
}