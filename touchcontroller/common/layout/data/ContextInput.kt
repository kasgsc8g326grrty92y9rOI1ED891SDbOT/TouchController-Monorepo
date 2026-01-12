package top.fifthlight.touchcontroller.common.layout.data

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import top.fifthlight.combine.item.data.Item
import top.fifthlight.touchcontroller.common.config.condition.BuiltinLayerCondition
import top.fifthlight.touchcontroller.common.config.condition.LayerConditionInput
import top.fifthlight.touchcontroller.common.gal.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.gamestate.CameraPerspective
import top.fifthlight.touchcontroller.common.gal.view.CrosshairTarget
import kotlin.uuid.Uuid

data class ContextInput(
    val inGui: Boolean = false,
    override val builtinCondition: PersistentSet<BuiltinLayerCondition> = persistentSetOf(),
    override val customCondition: PersistentSet<Uuid> = persistentSetOf(),
    val playerHandle: PlayerHandle? = null,
    val crosshairTarget: CrosshairTarget? = null,
    val perspective: CameraPerspective = CameraPerspective.FIRST_PERSON,
) : LayerConditionInput {
    companion object {
        val EMPTY = ContextInput()
    }

    override fun holdingItem(item: Item): Boolean = playerHandle?.matchesItemOnHand(item) ?: false
}
