package com.akira.undeadwave.listener

import com.akira.core.api.util.entity.MetadataEditor
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.arena.Arena
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent

class ArenaListener : Listener {
    @EventHandler
    fun onEnemyDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity is Player) return

        val metadata = MetadataEditor(UndeadWave.instance, entity).get("arena_name")
        val name = metadata?.asString() ?: return

        requireNotNull(
            Arena.PresetMap.container.values.firstOrNull { it.name == name }
        ) { "Unknown arena name from entity's metadata: $name. (EntityType: ${entity.type})" }
            .run { handleEnemyDeath(entity) }

        event.droppedExp = 0
        event.drops.clear()
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        Arena.PlayerMap.get(event.player)?.shutdown(false)
    }
}