package top.fifthlight.blazerod.model.animation

import top.fifthlight.blazerod.model.util.FloatWrapper
import top.fifthlight.blazerod.model.util.IntWrapper

interface AnimationContext {
    companion object {
        const val SECONDS_PER_TICK = 1f / 20f
    }

    interface Property<T> {
        object GameTick: Property<IntWrapper>
        object DeltaTick: Property<FloatWrapper>
    }

    // game ticks
    fun getGameTick(): Long
    // 0 ~ 1
    fun getDeltaTick(): Float

    fun <T> getProperty(type: Property<T>): T?

    fun getPropertyTypes(): List<Property<*>>
}
