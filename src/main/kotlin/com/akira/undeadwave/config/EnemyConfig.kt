package com.akira.undeadwave.config

import com.akira.core.api.config.ConfigFile
import com.akira.core.api.config.clear
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.enemy.Enemy

class EnemyConfig(plugin: UndeadWave, template: String) : ConfigFile(plugin, "enemy", template) {
    fun get(name: String): Enemy? {
        val section = config.getConfigurationSection(name) ?: return null
        return Enemy(name).apply { deserialize(section) }
    }

    fun load(name: String): Enemy = requireNotNull(this.get(name)) { "Enemy $name not found." }

    fun loadAll(): List<Enemy> = config.getKeys(false).map { load(it) }

    fun save(enemy: Enemy) {
        val name = enemy.name
        val section = config.getConfigurationSection(name) ?: config.createSection(name)

        enemy.serialize(section)
    }

    fun saveAll(enemies: Collection<Enemy>) {
        config.clear()
        enemies.forEach { save(it) }
    }
}