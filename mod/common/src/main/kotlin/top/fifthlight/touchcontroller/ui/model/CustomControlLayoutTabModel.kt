package top.fifthlight.touchcontroller.ui.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import org.koin.core.component.inject
import top.fifthlight.touchcontroller.config.ControllerLayout
import top.fifthlight.touchcontroller.config.LayoutLayer
import top.fifthlight.touchcontroller.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.config.preset.PresetConfig
import top.fifthlight.touchcontroller.config.preset.PresetManager
import top.fifthlight.touchcontroller.config.preset.builtin.BuiltinPresetKey
import top.fifthlight.touchcontroller.config.preset.builtin.preset
import top.fifthlight.touchcontroller.control.ControllerWidget
import top.fifthlight.touchcontroller.ext.combineStates
import top.fifthlight.touchcontroller.ext.fastRandomUuid
import top.fifthlight.touchcontroller.ui.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.ui.state.CustomControlLayoutTabState.Enabled.PageState
import kotlin.uuid.Uuid

class CustomControlLayoutTabModel(
    private val configScreenModel: ConfigScreenModel,
) : TouchControllerScreenModel() {
    private val presetManager: PresetManager by inject()
    private val pageState = MutableStateFlow(PageState())
    val uiState =
        combineStates(configScreenModel.uiState, presetManager.presets, pageState) { uiState, presets, selectState ->
            val config = uiState.config
            when (val preset = config.preset) {
                is PresetConfig.BuiltIn -> CustomControlLayoutTabState.Disabled
                is PresetConfig.Custom -> {
                    val selectedPreset = presets[preset.uuid]
                    val selectedLayer = selectedPreset?.layout?.getOrNull(selectState.selectedLayerIndex)
                    val selectedWidget = selectedLayer?.widgets?.getOrNull(selectState.selectedWidgetIndex)
                    CustomControlLayoutTabState.Enabled(
                        allPresets = presets,
                        selectedPresetUuid = preset.uuid,
                        selectedPreset = selectedPreset,
                        selectedLayer = selectedLayer,
                        selectedWidget = selectedWidget,
                        pageState = selectState,
                    )
                }
            }
        }

    fun enableCustomLayout() {
        configScreenModel.updateConfig {
            if (preset is PresetConfig.BuiltIn) {
                copy(preset = PresetConfig.Custom())
            } else {
                this
            }
        }
    }

    fun setShowSideBar(showSideBar: Boolean) {
        pageState.getAndUpdate { it.copy(showSideBar = showSideBar) }
    }

    fun setMoveLocked(moveLocked: Boolean) {
        pageState.getAndUpdate { it.copy(moveLocked = moveLocked) }
    }

    fun editPreset(uuid: Uuid, editor: LayoutPreset.() -> LayoutPreset) {
        val uiState = uiState.value as? CustomControlLayoutTabState.Enabled ?: return
        val preset = uiState.allPresets[uuid] ?: return
        val newPreset = editor(preset)
        presetManager.savePreset(uuid, newPreset)
    }

    fun selectPreset(uuid: Uuid) {
        pageState.getAndUpdate {
            it.copy(
                selectedLayerIndex = 0,
                selectedWidgetIndex = -1
            )
        }
        configScreenModel.updateConfig {
            copy(preset = PresetConfig.Custom(uuid))
        }
    }

    fun newPreset(preset: LayoutPreset? = null) {
        pageState.getAndUpdate {
            it.copy(
                selectedLayerIndex = 0,
                selectedWidgetIndex = -1
            )
        }
        val uuid = fastRandomUuid()
        val preset = preset ?: BuiltinPresetKey.DEFAULT.preset.copy(
            name = "New preset"
        )
        presetManager.savePreset(uuid, preset)
        configScreenModel.updateConfig {
            copy(preset = PresetConfig.Custom(uuid = uuid))
        }
    }

    fun deletePreset(uuid: Uuid) {
        pageState.getAndUpdate {
            it.copy(
                selectedLayerIndex = 0,
                selectedWidgetIndex = -1
            )
        }
        presetManager.removePreset(uuid)
        configScreenModel.updateConfig {
            if (preset is PresetConfig.Custom && preset.uuid == uuid) {
                copy(preset = PresetConfig.Custom())
            } else {
                this
            }
        }
    }

    fun copyWidget(widget: ControllerWidget) {
        pageState.getAndUpdate { it.copy(copiedWidget = widget) }
    }

    fun selectWidget(index: Int) {
        pageState.getAndUpdate { it.copy(selectedWidgetIndex = index) }
    }

    fun editLayer(action: LayoutLayer.() -> LayoutLayer) {
        val uiState = uiState.value as? CustomControlLayoutTabState.Enabled ?: return
        val selectedPreset = uiState.selectedPreset ?: return
        val selectedPresetUuid = uiState.selectedPresetUuid ?: return
        val selectedLayerIndex =
            uiState.pageState.selectedLayerIndex.takeIf { it in selectedPreset.layout.indices } ?: return
        editPreset(selectedPresetUuid) {
            copy(
                layout = ControllerLayout(
                    layout.set(selectedLayerIndex, action(layout[selectedLayerIndex]))
                )
            )
        }
    }

    fun deleteLayer(index: Int) {
        pageState.getAndUpdate {
            it.copy(
                selectedWidgetIndex = -1,
                selectedLayerIndex = -1,
            )
        }
        val uiState = uiState.value as? CustomControlLayoutTabState.Enabled ?: return
        val selectedPresetUuid = uiState.selectedPresetUuid ?: return
        editPreset(selectedPresetUuid) {
            copy(layout = ControllerLayout(layout.removeAt(index)))
        }
    }

    fun selectLayer(index: Int) {
        pageState.getAndUpdate {
            it.copy(
                selectedWidgetIndex = -1,
                selectedLayerIndex = index,
            )
        }
    }

    fun editWidget(index: Int, widget: ControllerWidget) {
        editLayer { copy(widgets = widgets.set(index, widget)) }
    }

    fun newWidget(widget: ControllerWidget): Int {
        val newWidget = widget.newId()
        var index = 0
        editLayer { copy(widgets = widgets.add(newWidget)).also { index = it.widgets.size - 1 } }
        return index
    }

    fun deleteWidget(index: Int) {
        pageState.getAndUpdate {
            it.copy(
                selectedWidgetIndex = -1
            )
        }
        editLayer { copy(widgets = widgets.removeAt(index)) }
    }
}