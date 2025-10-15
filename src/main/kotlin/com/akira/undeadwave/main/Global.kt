package com.akira.undeadwave.main

import com.akira.core.api.util.general.ValidateFeedback
import com.akira.undeadwave.UndeadWave
import com.akira.undeadwave.main.enemy.Enemy

object Global {
    var enabled = false
    val disabled get() = !enabled

    fun validate(): ValidateFeedback {
        val result = ValidateFeedback()
        val feedback = GlobalSettings.validate()

        if (feedback.failed) feedback.content.forEach(result::add)
        if (Enemy.container.isEmpty()) result.add("配置文件需定义至少一种怪物。")

        return result
    }

    fun selfCheck() {
        val plugin = UndeadWave.instance
        val feedback = this.validate()

        if (feedback.passed) {
            plugin.logInfo("全局自检通过，游戏已正常启用。")
            enabled = true
        } else {
            plugin.logWarn("全局自检未通过，参考信息：")
            feedback.content.forEach { plugin.logWarn("- $it") }
        }
    }
}