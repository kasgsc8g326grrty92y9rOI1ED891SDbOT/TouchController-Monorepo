package top.fifthlight.blazerod.animation.context

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderTickCounter
import top.fifthlight.blazerod.mixin.MinecraftClientAccessor
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.util.MutableFloat
import top.fifthlight.blazerod.model.util.MutableLong

open class BaseAnimationContext(
    protected val client: MinecraftClient = MinecraftClient.getInstance(),
) : AnimationContext {
    protected val tickCounter: RenderTickCounter = client.renderTickCounter

    private val gameTickValue = MutableLong()
    override fun getGameTick(): Long = (client as MinecraftClientAccessor).uptimeInTicks

    private val deltaTickValue = MutableFloat()
    override fun getDeltaTick(): Float = tickCounter.getTickProgress(false)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getProperty(type: AnimationContext.Property<T>): T? = when (type) {
        AnimationContext.Property.GameTick -> gameTickValue.apply { value = getGameTick() }
        AnimationContext.Property.DeltaTick -> deltaTickValue.apply { value = getDeltaTick() }
        else -> null
    } as T?

    companion object {
        protected val propertyTypes = listOf(
            AnimationContext.Property.GameTick,
            AnimationContext.Property.DeltaTick,
        )

        val instance = BaseAnimationContext()
    }

    override fun getPropertyTypes(): List<AnimationContext.Property<*>> = propertyTypes
}