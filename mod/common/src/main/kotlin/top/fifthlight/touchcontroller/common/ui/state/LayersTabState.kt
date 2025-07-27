package top.fifthlight.touchcontroller.common.ui.state

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import top.fifthlight.touchcontroller.common.config.*
import top.fifthlight.touchcontroller.common.config.LayoutLayer.Companion.DEFAULT_LAYER_NAME
import top.fifthlight.touchcontroller.common.config.preset.CustomCondition
import top.fifthlight.touchcontroller.common.config.preset.CustomConditions
import kotlin.uuid.Uuid

sealed class LayersTabState {
    data object Empty : LayersTabState()

    data class Create(
        val name: String = DEFAULT_LAYER_NAME,
        val condition: PersistentMap<LayerConditionKey, LayerConditionValue> = persistentMapOf(),
        val customConditions: PersistentMap<Uuid, LayerConditionValue> = persistentMapOf(),
    ) : LayersTabState() {
        fun toLayer() = LayoutLayer(
            name = name,
            condition = LayoutLayerCondition(condition),
            customConditions = LayoutLayerCustomCondition(customConditions),
        )
    }

    data class Edit(
        val index: Int,
        val name: String,
        val condition: PersistentMap<LayerConditionKey, LayerConditionValue>,
        val customConditions: PersistentMap<Uuid, LayerConditionValue>,
        val customConditionState: CustomConditionState? = null,
    ) : LayersTabState() {
        constructor(index: Int, layer: LayoutLayer) : this(
            index = index,
            name = layer.name,
            condition = layer.condition,
            customConditions = layer.customConditions,
        )

        data class CustomConditionState(
            val conditions: CustomConditions,
            val editData: EditData? = null,
        ) {
            data class EditData(
                val index: Int,
                val condition: CustomCondition,
            )
        }

        fun edit(layer: LayoutLayer) = layer.copy(
            name = name,
            condition = LayoutLayerCondition(condition),
            customConditions = LayoutLayerCustomCondition(customConditions),
        )
    }

    data class Delete(
        val index: Int,
        val layer: LayoutLayer,
    ) : LayersTabState()
}
