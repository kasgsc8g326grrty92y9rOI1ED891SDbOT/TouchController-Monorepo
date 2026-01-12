package top.fifthlight.touchcontroller.common.config.layout

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.config.serializer.LayoutLayerSerializer
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import kotlin.let

@Serializable(with = LayoutLayerSerializer::class)
data class LayoutLayer(
    val name: String = DEFAULT_LAYER_NAME,
    val widgets: PersistentList<ControllerWidget> = persistentListOf(),
    val conditions: LayerConditions = LayerConditions(),
) {
    companion object {
        const val DEFAULT_LAYER_NAME = "Unnamed layer"
    }

    operator fun plus(widget: ControllerWidget?) = widget?.let {
        copy(widgets = widgets + widget.newId())
    } ?: this
}
