package top.fifthlight.blazerod.model.animation

data class SimpleAnimation(
    override val name: String? = null,
    override val channels: List<KeyFrameAnimationChannel<*, *>>,
) : Animation {
    override val duration = channels.maxOf { it.duration }

    override fun createState(context: AnimationContext) = SimpleAnimationState(context, duration)
}