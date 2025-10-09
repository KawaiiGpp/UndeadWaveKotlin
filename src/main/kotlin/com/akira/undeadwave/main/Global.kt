package com.akira.undeadwave.main

import com.akira.undeadwave.UndeadWave

object Global {
    var enabled = false

    fun performSelfCheck() {
        val feedback = GlobalSettings.validate()
        val plugin = UndeadWave.instance

        if (feedback.failed) {
            plugin.logWarn("自检未通过，玩家暂无法加入游戏。")
            feedback.content.forEach(plugin::logWarn)
        } else {
            plugin.logInfo("自检已通过，玩家可以正常加入游戏。")
            enabled = true
        }
    }
}