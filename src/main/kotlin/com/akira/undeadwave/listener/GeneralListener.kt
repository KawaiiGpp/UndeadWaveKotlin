package com.akira.undeadwave.listener

import com.akira.undeadwave.main.arena.Arena
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class GeneralListener : Listener {
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) = Arena.PlayerMap.get(event.player)?.shutdown(false)
}