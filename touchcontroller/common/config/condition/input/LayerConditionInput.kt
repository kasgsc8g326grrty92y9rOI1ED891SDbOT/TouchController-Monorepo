package top.fifthlight.touchcontroller.common.config.condition

import top.fifthlight.combine.item.data.Item
import kotlin.uuid.Uuid

interface LayerConditionInput {
    val builtinCondition: Set<BuiltinLayerCondition>
    val customCondition: Set<Uuid>
    fun holdingItem(item: Item): Boolean
}