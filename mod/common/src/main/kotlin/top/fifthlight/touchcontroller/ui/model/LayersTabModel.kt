package top.fifthlight.touchcontroller.ui.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import top.fifthlight.touchcontroller.config.ControllerLayout
import top.fifthlight.touchcontroller.config.LayoutLayer
import top.fifthlight.touchcontroller.ui.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.ui.state.LayersTabState

class LayersTabModel(
    private val screenModel: CustomControlLayoutTabModel,
): TouchControllerScreenModel() {
    private val _uiState = MutableStateFlow<LayersTabState>(LayersTabState.Empty)
    val uiState = _uiState.asStateFlow()

    fun clearState() {
        _uiState.value = LayersTabState.Empty
    }

    fun openCreateLayerDialog() {
        _uiState.value = LayersTabState.Create()
    }

    fun updateCreateLayerState(editor: LayersTabState.Create.() -> LayersTabState.Create) {
        _uiState.getAndUpdate {
            if (it is LayersTabState.Create) {
                editor(it)
            } else {
                it
            }
        }
    }

    fun createLayer(state: LayersTabState.Create) {
        val currentPresetUuid = screenModel.uiState.value as? CustomControlLayoutTabState.Enabled ?: return
        val presetUuid = currentPresetUuid.selectedPresetUuid ?: return
        val layer = state.toLayer()
        screenModel.editPreset(presetUuid) {
            copy(layout = ControllerLayout(layout.layers.add(layer)))
        }
        clearState()
    }

    fun openEditLayerDialog(index: Int, layer: LayoutLayer) {
        _uiState.value = LayersTabState.Edit(index, layer)
    }

    fun updateEditLayerState(editor: LayersTabState.Edit.() -> LayersTabState.Edit) {
        _uiState.getAndUpdate {
            if (it is LayersTabState.Edit) {
                editor(it)
            } else {
                it
            }
        }
    }

    fun openDeleteLayerDialog(index: Int, layer: LayoutLayer) {
        _uiState.value = LayersTabState.Delete(
            index = index,
            layer = layer
        )
    }

    fun copyLayer(layer: LayoutLayer) {
        val uiState = screenModel.uiState.value as? CustomControlLayoutTabState.Enabled ?: return
        val uuid = uiState.selectedPresetUuid ?: return
        screenModel.editPreset(uuid) {
            copy(layout = ControllerLayout(layout.layers.add(layer)))
        }
    }

    fun moveLayer(index: Int, offset: Int) {
        val uiState = screenModel.uiState.value as? CustomControlLayoutTabState.Enabled ?: return
        val uuid = uiState.selectedPresetUuid ?: return
        screenModel.editPreset(uuid) {
            val layer = layout.layers[index]
            val newIndex = (index + offset).coerceIn(layout.layers.indices)
            val newLayers = layout.layers.removeAt(index).add(newIndex, layer)
            copy(layout = ControllerLayout(newLayers))
        }
    }
}