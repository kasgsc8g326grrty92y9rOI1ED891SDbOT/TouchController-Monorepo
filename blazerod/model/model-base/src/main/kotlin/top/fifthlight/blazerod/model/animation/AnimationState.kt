package top.fifthlight.blazerod.model.animation

interface AnimationState {
    fun updateTime(context: AnimationContext)

    fun getTime(): Float
}
