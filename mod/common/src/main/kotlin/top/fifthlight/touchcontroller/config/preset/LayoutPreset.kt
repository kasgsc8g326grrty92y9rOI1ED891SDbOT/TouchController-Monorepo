package top.fifthlight.touchcontroller.config.preset

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.config.ControllerLayout
import top.fifthlight.touchcontroller.control.ControllerWidget
import top.fifthlight.touchcontroller.ext.LayoutPresetsSerializer

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

    companion object {
        const val DEFAULT_PRESET_NAME = "Unnamed preset"
    }
}

@Immutable
@Serializable
data class PresetControlInfo(
    val splitControls: Boolean = false,
    val disableTouchGesture: Boolean = false,
)

@JvmInline
@Serializable(with = LayoutPresetsSerializer::class)
value class LayoutPresets(
    val presets: PersistentList<LayoutPreset> = persistentListOf(),
)

fun layoutPresetsOf(vararg pairs: LayoutPreset) = LayoutPresets(persistentListOf(*pairs))
