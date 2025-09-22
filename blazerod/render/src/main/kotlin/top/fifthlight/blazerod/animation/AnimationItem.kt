package top.fifthlight.blazerod.animation

import top.fifthlight.blazerod.model.ModelInstance
import top.fifthlight.blazerod.model.animation.Animation
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.animation.AnimationState

data class AnimationItem(
    val name: String? = null,
    val animation: Animation,
    val channels: List<AnimationChannelItem<*, *>>,
) {
    val duration
        get() = animation.duration

    fun createState(context: AnimationContext) = animation.createState(context)

    fun apply(instance: ModelInstance, context: AnimationContext, state: AnimationState) = channels.forEach {
        it.apply(instance, context, state)
    }
}
