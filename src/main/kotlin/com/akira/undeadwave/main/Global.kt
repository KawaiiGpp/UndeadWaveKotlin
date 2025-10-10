package com.akira.undeadwave.main

import com.akira.undeadwave.UndeadWave

object Global {
    var enabled = false
    val disabled get() = !enabled

    fun performSelfCheck() {
        val feedback = GlobalSettings.validate()
        val plugin = UndeadWave.instance

        if (feedback.failed) {
            plugin.logWarn("全局配置自检未通过，参考信息：")
            feedback.content.forEach { plugin.logWarn("- $it") }
        } else {
            plugin.logInfo("全局配置自检已通过。")
            enabled = true
        }
    }
}