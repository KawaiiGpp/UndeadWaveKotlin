package com.akira.undeadwave.main.arena

import com.akira.core.api.Manager
import com.akira.core.api.util.entity.MetadataEditor
import com.akira.core.api.util.world.worldNonNull
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.Global
import com.akira.undeadwave.main.GlobalSettings
import com.akira.undeadwave.util.restore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Ageable
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie

class Arena(val preset: ArenaPreset, val session: ArenaSession) {
    val name get() = preset.name
    var state = ArenaState.FREE; private set

    fun start() {
        require(Global.enabled) { "Game not enabled." }
        require(state == ArenaState.FREE) { "Arena must be in free state." }

        state = ArenaState.WORKING
        PlayerMap.register(session.player, this)
        PresetMap.register(preset, this)

        session.player.restore()
        session.player.sendMessage { Component.text("游戏开始了！", NamedTextColor.GREEN) }
        session.player.teleport(preset.spawnpoint.value)

        this.handleNext()
    }

    fun shutdown(victory: Boolean) {
        require(state == ArenaState.WORKING) { "Arena is not in working state." }

        session.player.restore()
        session.player.sendMessage { Component.text("游戏结束！胜利状态=$victory。", NamedTextColor.GREEN) }
        session.player.teleport(GlobalSettings.lobby.value)

        session.enemies.forEach {
            val entity = Bukkit.getEntity(it)

            if (entity != null) entity.remove()
            else UndeadWave.instance.logError("实体未找到：$it")
        }

        PresetMap.unregister(preset)
        PlayerMap.unregister(session.player)
        state = ArenaState.SHUTDOWN
    }

    fun spawnEnemies() {
        repeat(session.round * preset.enemyMultiplier.value) {
            val clazz: Class<out LivingEntity> = Zombie::class.java
            val location = preset.enemySpawnpoints.value.random()
            val entity = location.worldNonNull.spawn(location, clazz)

            entity.removeWhenFarAway = false
            entity.removeWhenFarAway = false
            entity.maximumNoDamageTicks = 0
            entity.isPersistent = true
            entity.canPickupItems = false

            if (entity is Ageable) entity.setAdult()
            entity.vehicle?.remove()

            session.markEnemy(entity)
            MetadataEditor(UndeadWave.instance, entity).set("arena_name", preset.name)
        }
    }

    fun handleEnemyDeath(entity: LivingEntity) {
        session.unmarkEnemy(entity)

        if (session.hasEnemiesLeft()) return
        this.handleNext()
    }

    fun handleNext() {
        if (session.round < preset.totalRounds.value) {
            session.increaseRound()
            session.player.sendMessage { Component.text("第 ${session.round} 回合已经开始！", NamedTextColor.AQUA) }

            this.spawnEnemies()
        } else shutdown(true)
    }

    object PlayerMap : Manager<Player, Arena>()

    object PresetMap : Manager<ArenaPreset, Arena>()
}