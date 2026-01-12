package top.fifthlight.touchcontroller.common.config.preset

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.ControllerLayout
import top.fifthlight.touchcontroller.common.config.LayoutLayer
import top.fifthlight.touchcontroller.common.control.ControllerWidget

@Immutable
@Serializable
data class LayoutPreset(
    val name: String = DEFAULT_PRESET_NAME,
    val controlInfo: PresetControlInfo = PresetControlInfo(),
    val layout: ControllerLayout = ControllerLayout(),
) {
    fun mapWidgets(transform: (ControllerWidget) -> ControllerWidget) = copy(
        layout = ControllerLayout(layout.layers.map { layer ->
            layer.copy(widgets = layer.widgets.map(transform).toPersistentList())
        }.toPersistentList())
    )

    fun mapLayers(transform: (LayoutLayer) -> LayoutLayer) = copy(
        layout = ControllerLayout(layers = layout.layers.map(transform).toPersistentList()),
    )

    companion object {
        const val DEFAULT_PRESET_NAME = "Unnamed preset"
    }
}
