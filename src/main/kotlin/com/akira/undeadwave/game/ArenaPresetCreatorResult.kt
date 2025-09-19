package com.akira.undeadwave.game

class ArenaPresetCreatorResult
private constructor(
    private val failureMessageRaw: String?,
    private val productRaw: ArenaPreset?
) {
    val success get() = productRaw != null
    val failure get() = failureMessageRaw != null

    val failureMessage get() = requireNotNull(failureMessageRaw) { "No failure messages from result." }
    val product get() = requireNotNull(productRaw) { "No products from result." }

    companion object {
        fun createSuccess(product: ArenaPreset) = ArenaPresetCreatorResult(null, product)

        fun createFailure(message: String) = ArenaPresetCreatorResult(message, null)
    }
}