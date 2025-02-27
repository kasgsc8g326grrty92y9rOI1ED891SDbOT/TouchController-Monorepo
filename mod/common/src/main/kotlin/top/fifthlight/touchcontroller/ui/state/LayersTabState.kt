package top.fifthlight.touchcontroller.ui.state

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import top.fifthlight.touchcontroller.config.LayerConditionKey
import top.fifthlight.touchcontroller.config.LayerConditionValue
import top.fifthlight.touchcontroller.config.LayoutLayer
import top.fifthlight.touchcontroller.config.LayoutLayer.Companion.DEFAULT_LAYER_NAME
import top.fifthlight.touchcontroller.config.LayoutLayerCondition

sealed class LayersTabState {
    data object Empty : LayersTabState()

    data class Create(
        val name: String = DEFAULT_LAYER_NAME,
        val condition: PersistentMap<LayerConditionKey, LayerConditionValue> = persistentMapOf(),
    ) : LayersTabState() {
        fun toLayer() = LayoutLayer(
            name = name,
            condition = LayoutLayerCondition(condition),
        )
    }

    data class Edit(
        val index: Int,
        val name: String,
        val condition: PersistentMap<LayerConditionKey, LayerConditionValue>,
    ) : LayersTabState() {
        constructor(index: Int, layer: LayoutLayer) : this(
            index = index,
            name = layer.name,
            condition = layer.condition,
        )

        fun edit(layer: LayoutLayer) = layer.copy(
            name = name,
            condition = LayoutLayerCondition(condition),
        )
    }

    data class Delete(
        val index: Int,
        val layer: LayoutLayer,
    ) : LayersTabState()
}
