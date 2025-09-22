package top.fifthlight.armorstand.state

import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.entity.EntityPose
import net.minecraft.entity.EntityType
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import top.fifthlight.armorstand.extension.internal.PlayerEntityRenderStateExtInternal
import top.fifthlight.armorstand.util.toRadian
import top.fifthlight.armorstand.vmc.VmcMarionetteManager
import top.fifthlight.blazerod.animation.AnimationItem
import top.fifthlight.blazerod.model.*
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.animation.AnimationState
import top.fifthlight.blazerod.model.resource.RenderExpression
import top.fifthlight.blazerod.model.resource.RenderExpressionGroup
import java.util.*
import kotlin.math.PI
import kotlin.math.sin

sealed interface ModelController {
    fun update(uuid: UUID, vanillaState: PlayerEntityRenderState, context: AnimationContext) = Unit
    fun apply(context: AnimationContext, instance: ModelInstance)

    private class JointItem(
        private val nodeIndex: Int,
    ) {
        fun update(instance: ModelInstance, func: NodeTransform.Decomposed.() -> Unit) {
            instance.setTransformDecomposed(nodeIndex, TransformId.RELATIVE_ANIMATION, func)
        }

        fun updateAbsolute(instance: ModelInstance, func: NodeTransform.Decomposed.() -> Unit) {
            instance.setTransformDecomposed(nodeIndex, TransformId.ABSOLUTE, func)
        }
    }

    private sealed class ExpressionItem {
        abstract fun apply(instance: ModelInstance, weight: Float)

        fun RenderExpression.apply(instance: ModelInstance, weight: Float) = bindings.forEach { binding ->
            when (binding) {
                is RenderExpression.Binding.MorphTarget -> {
                    instance.setGroupWeight(binding.morphedPrimitiveIndex, binding.groupIndex, weight)
                }
            }
        }

        data class Expression(
            val expression: RenderExpression,
        ) : ExpressionItem() {
            override fun apply(instance: ModelInstance, weight: Float) = expression.apply(instance, weight)
        }

        data class Group(
            val group: RenderExpressionGroup,
        ) : ExpressionItem() {
            override fun apply(instance: ModelInstance, weight: Float) = group.items.forEach { item ->
                val expression = instance.scene.expressions[item.expressionIndex]
                expression.apply(instance, weight * item.influence)
            }
        }
    }

    companion object {
        private fun RenderScene.getBone(tag: HumanoidTag) =
            humanoidTagMap[tag]?.let { node -> JointItem(nodeIndex = node.nodeIndex) }

        private fun RenderScene.getExpression(tag: Expression.Tag) =
            expressions.firstOrNull { it.tag == tag }?.let { ExpressionItem.Expression(it) }
                ?: expressionGroups.firstOrNull { it.tag == tag }?.let { ExpressionItem.Group(it) }

        private const val NANOSECONDS_PER_SECOND = 1_000_000_000L

        fun calculateBlinkProgress(
            playerUuid: UUID,
            averageBlinkInterval: Long,
            blinkDuration: Long,
            currentTime: Long,
        ): Float {
            val seed1 = playerUuid.mostSignificantBits
            val seed2 = playerUuid.leastSignificantBits
            val seed = seed1 xor seed2

            val offsetMillis = (seed % (averageBlinkInterval * 2)).coerceAtLeast(0)
            val effectiveTime = currentTime + offsetMillis
            val cycleProgress = effectiveTime % averageBlinkInterval
            return if (cycleProgress < blinkDuration) {
                val phase = (cycleProgress.toFloat() / blinkDuration.toFloat()) * MathHelper.PI
                sin(phase)
            } else {
                0f
            }
        }

        fun calculateBlinkProgress(
            playerUuid: UUID,
            averageBlinkInterval: Double,
            blinkDuration: Double,
            currentTime: Long,
        ) = calculateBlinkProgress(
            playerUuid,
            (averageBlinkInterval * NANOSECONDS_PER_SECOND).toLong(),
            (blinkDuration * NANOSECONDS_PER_SECOND).toLong(),
            currentTime,
        )
    }

    class LiveUpdated private constructor(
        private val center: JointItem?,
        private val head: JointItem?,
        private val blinkExpression: ExpressionItem?,
    ) : ModelController {
        private var bodyYaw: Float = 0f
        private var headYaw: Float = 0f
        private var headPitch: Float = 0f
        private var blinkProgress: Float = 0f

        constructor(
            scene: RenderScene,
        ) : this(
            center = scene.getBone(HumanoidTag.HIPS),
            head = scene.getBone(HumanoidTag.HEAD),
            blinkExpression = scene.getExpression(Expression.Tag.BLINK),
        )

        override fun update(uuid: UUID, vanillaState: PlayerEntityRenderState, context: AnimationContext) {
            bodyYaw = MathHelper.PI - vanillaState.bodyYaw.toRadian()
            headYaw = -vanillaState.relativeHeadYaw.toRadian()
            headPitch = -vanillaState.pitch.toRadian()
            blinkProgress = calculateBlinkProgress(
                playerUuid = uuid,
                averageBlinkInterval = 4.0,
                blinkDuration = 0.25,
                currentTime = System.nanoTime(),
            )
        }

        override fun apply(context: AnimationContext, instance: ModelInstance) {
            center?.update(instance) {
                rotation.rotationY(bodyYaw)
            }
            head?.update(instance) {
                rotation.rotationYXZ(headYaw, headPitch, 0f)
            }
            blinkExpression?.apply(instance, blinkProgress)
        }
    }

    interface AnimatedModelController : ModelController {
        val animationState: AnimationState
    }

    class Predefined(
        context: AnimationContext,
        private val animation: AnimationItem,
    ) : AnimatedModelController {
        override val animationState: AnimationState = animation.createState(context)

        override fun apply(context: AnimationContext, instance: ModelInstance) {
            animationState.updateTime(context)
            animation.apply(instance, context, animationState)
        }
    }

    class LiveSwitched private constructor(
        context: AnimationContext,
        private val animationSet: FullAnimationSet,
        private val head: JointItem?,
        private val blinkExpression: ExpressionItem?,
    ) : AnimatedModelController {
        constructor(
            context: AnimationContext,
            scene: RenderScene,
            animationSet: FullAnimationSet,
        ) : this(
            context = context,
            animationSet = animationSet,
            head = scene.getBone(HumanoidTag.HEAD),
            blinkExpression = scene.getExpression(Expression.Tag.BLINK),
        )

        sealed class PlayState {
            abstract fun getItem(set: FullAnimationSet): AnimationItem
            open val loop: Boolean = true

            data object Idle : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.idle
            }

            data object Walking : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.walk
            }

            data object ElytraFly : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.elytraFly
            }

            data object Swimming : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.swim
            }

            data object Sleeping : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.sleep
            }

            data object Riding : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.ride
            }

            data object OnHorse : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.onHorse
            }

            data object Dying : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.die
                override val loop: Boolean
                    get() = false
            }

            data object Sprinting : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.sprint
            }

            data object Sneaking : PlayState() {
                override fun getItem(set: FullAnimationSet) = set.sneak
            }
        }

        private var playState: PlayState = PlayState.Idle
        override var animationState: AnimationState = playState.getItem(animationSet).createState(context)
        private var item: AnimationItem? = null
        private var reset = false
        private var bodyYaw: Float = 0f
        private var headYaw: Float = 0f
        private var headPitch: Float = 0f
        private var blinkProgress: Float = 0f

        companion object {
            private val PlayerEntityRenderState.vehicleType
                get() = (this as PlayerEntityRenderStateExtInternal).`armorstand$getRidingEntityType`()
            private val PlayerEntityRenderState.isSprinting
                get() = (this as PlayerEntityRenderStateExtInternal).`armorstand$isSprinting`()
            private val PlayerEntityRenderState.isDead
                get() = (this as PlayerEntityRenderStateExtInternal).`armorstand$isDead`()
            private val PlayerEntityRenderState.limbSwingSpeed
                get() = (this as PlayerEntityRenderStateExtInternal).`armorstand$getLimbSwingSpeed`()
            private val horseEntityTypes = listOf(
                EntityType.HORSE,
                EntityType.DONKEY,
                EntityType.MULE,
                EntityType.LLAMA,
                EntityType.SKELETON_HORSE,
                EntityType.ZOMBIE_HORSE,
            )
        }

        private fun getState(vanillaState: PlayerEntityRenderState): PlayState = when {
            vanillaState.vehicleType in horseEntityTypes -> PlayState.OnHorse
            vanillaState.vehicleType != null -> PlayState.Riding
            vanillaState.isDead -> PlayState.Dying
            vanillaState.pose == EntityPose.CROUCHING -> PlayState.Sneaking
            vanillaState.pose == EntityPose.GLIDING -> PlayState.ElytraFly
            vanillaState.pose == EntityPose.SLEEPING -> PlayState.Sleeping
            vanillaState.pose == EntityPose.SWIMMING -> PlayState.Swimming

            else -> if (vanillaState.isSprinting) {
                PlayState.Sprinting
            } else if (vanillaState.limbSwingSpeed > .4f) {
                PlayState.Walking
            } else {
                PlayState.Idle
            }
        }

        override fun update(
            uuid: UUID,
            vanillaState: PlayerEntityRenderState,
            context: AnimationContext,
        ) {
            val sleepingDirection = vanillaState.sleepingDirection
            bodyYaw = if (vanillaState.isInPose(EntityPose.SLEEPING) && sleepingDirection != null) {
                when (sleepingDirection) {
                    Direction.SOUTH -> 0f
                    Direction.EAST -> PI.toFloat() * 0.5f
                    Direction.NORTH -> PI.toFloat()
                    Direction.WEST -> PI.toFloat() * 1.5f
                    else -> 0f
                }
            } else {
                MathHelper.PI - vanillaState.bodyYaw.toRadian()
            }
            headYaw = -vanillaState.relativeHeadYaw.toRadian()
            headPitch = -vanillaState.pitch.toRadian()
            blinkProgress = calculateBlinkProgress(
                playerUuid = uuid,
                averageBlinkInterval = 4.0,
                blinkDuration = 0.25,
                currentTime = System.nanoTime(),
            )
            val newState = getState(vanillaState)
            if (newState != playState) {
                this.playState = newState
            }
            val newItem = newState.getItem(animationSet)
            if (newItem != item) {
                animationState = newItem.createState(context)
                item = newItem
                reset = true
            }
            animationState.updateTime(context)
        }

        override fun apply(context: AnimationContext, instance: ModelInstance) {
            val item = item ?: return
            if (reset) {
                instance.clearTransform()
                reset = false
            }
            item.apply(instance, context, animationState)
            instance.setTransformDecomposed(instance.scene.rootNode.nodeIndex, TransformId.RELATIVE_ANIMATION) {
                rotation.rotationY(bodyYaw)
            }
            head?.update(instance) {
                rotation.rotationYXZ(headYaw, headPitch, 0f)
            }
            blinkExpression?.apply(instance, blinkProgress)
        }
    }

    class Vmc(
        private val scene: RenderScene,
    ) : ModelController {
        private val bones = mutableMapOf<HumanoidTag, Optional<JointItem>>()
        private val expressions = mutableMapOf<Expression.Tag, Optional<ExpressionItem>>()

        override fun apply(context: AnimationContext, instance: ModelInstance) {
            val state = VmcMarionetteManager.getState() ?: return
            state.rootTransform?.let {
                instance.setTransformDecomposed(scene.rootNode.nodeIndex, TransformId.ABSOLUTE) {
                    translation.set(it.position)
                    rotation.set(it.rotation)
                }
            }
            state.boneTransforms.forEach { (bone, value) ->
                val item = bones.getOrPut(bone) { Optional.ofNullable(scene.getBone(bone)) }
                item.ifPresent {
                    it.updateAbsolute(instance) {
                        translation.set(value.position)
                        rotation.set(value.rotation)
                    }
                }
            }
            state.blendShapes.forEach { (tag, value) ->
                val item = expressions.getOrPut(tag) { Optional.ofNullable(scene.getExpression(tag)) }
                item.ifPresent {
                    it.apply(instance, value)
                }
            }
        }
    }
}
