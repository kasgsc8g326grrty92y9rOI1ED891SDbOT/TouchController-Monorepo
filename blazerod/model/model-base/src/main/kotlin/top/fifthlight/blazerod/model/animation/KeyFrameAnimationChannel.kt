package top.fifthlight.blazerod.model.animation

import org.joml.Quaternionf
import org.joml.Vector3f
import top.fifthlight.blazerod.model.util.MutableFloat

data class KeyFrameAnimationChannel<T : Any, D>(
    override val type: AnimationChannel.Type<T, D>,
    override val typeData: D,
    private val components: List<AnimationChannelComponent<*, *>> = listOf(),
    val indexer: AnimationKeyFrameIndexer,
    val interpolator: AnimationInterpolator<T>,
    val keyframeData: AnimationKeyFrameData<T>,
    val interpolation: AnimationInterpolation,
    val defaultValue: () -> T,
) : AnimationChannel<T, D> {
    init {
        require(interpolation.elements == keyframeData.elements) { "Bad elements of keyframe data: ${keyframeData.elements}" }
    }

    val duration: Float
        get() = indexer.lastTime

    private val indexResult = AnimationKeyFrameIndexer.FindResult()
    private val startValues = List(interpolation.elements) { defaultValue() }
    private val endValues = List(interpolation.elements) { defaultValue() }

    private val componentOfTypes = components.associateBy { it.type }

    init {
        for (component in componentOfTypes.values) {
            component.onAttachToChannel(this)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : AnimationChannelComponent.Type<C, T>, C> getComponent(type: T): C? =
        componentOfTypes[type] as C?

    override fun getData(context: AnimationContext, state: AnimationState, result: T) {
        val time = state.getTime()
        indexer.findKeyFrames(time, indexResult)
        if (indexResult.startFrame == indexResult.endFrame || indexResult.startTime > time || indexResult.endTime < time) {
            keyframeData.get(indexResult.startFrame, startValues)
            interpolator.set(startValues, result)
            return
        }
        val delta = (time - indexResult.startTime) / (indexResult.endTime - indexResult.startTime)
        keyframeData.get(indexResult.startFrame, startValues)
        keyframeData.get(indexResult.endFrame, endValues)
        interpolator.interpolate(
            delta = delta,
            startFrame = indexResult.startFrame,
            endFrame = indexResult.endFrame,
            type = interpolation,
            startValue = startValues,
            endValue = endValues,
            result = result,
        )
    }
}

@JvmName("Vector3fKeyFrameAnimationChannel")
fun <D> KeyFrameAnimationChannel(
    type: AnimationChannel.Type<Vector3f, D>,
    data: D,
    components: List<AnimationChannelComponent<*, *>> = listOf(),
    indexer: AnimationKeyFrameIndexer,
    keyframeData: AnimationKeyFrameData<Vector3f>,
    interpolation: AnimationInterpolation,
): KeyFrameAnimationChannel<Vector3f, D> = KeyFrameAnimationChannel(
    type = type,
    typeData = data,
    components = components,
    indexer = indexer,
    interpolator = Vector3AnimationInterpolator,
    keyframeData = keyframeData,
    interpolation = interpolation,
    defaultValue = ::Vector3f,
)

@JvmName("QuaternionfKeyFrameAnimationChannel")
fun <D> KeyFrameAnimationChannel(
    type: AnimationChannel.Type<Quaternionf, D>,
    data: D,
    components: List<AnimationChannelComponent<*, *>> = listOf(),
    indexer: AnimationKeyFrameIndexer,
    keyframeData: AnimationKeyFrameData<Quaternionf>,
    interpolation: AnimationInterpolation,
): KeyFrameAnimationChannel<Quaternionf, D> = KeyFrameAnimationChannel(
    type = type,
    typeData = data,
    components = components,
    indexer = indexer,
    interpolator = QuaternionAnimationInterpolator,
    keyframeData = keyframeData,
    interpolation = interpolation,
    defaultValue = ::Quaternionf,
)

@JvmName("FloatKeyFrameAnimationChannel")
fun <D> KeyFrameAnimationChannel(
    type: AnimationChannel.Type<MutableFloat, D>,
    data: D,
    components: List<AnimationChannelComponent<*, *>> = listOf(),
    indexer: AnimationKeyFrameIndexer,
    keyframeData: AnimationKeyFrameData<MutableFloat>,
    interpolation: AnimationInterpolation,
): KeyFrameAnimationChannel<MutableFloat, D> = KeyFrameAnimationChannel(
    type = type,
    typeData = data,
    components = components,
    indexer = indexer,
    interpolator = FloatAnimationInterpolator,
    keyframeData = keyframeData,
    interpolation = interpolation,
    defaultValue = ::MutableFloat,
)
