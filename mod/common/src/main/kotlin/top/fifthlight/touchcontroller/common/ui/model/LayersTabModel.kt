package top.fifthlight.touchcontroller.common.ui.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import top.fifthlight.touchcontroller.common.config.ControllerLayout
import top.fifthlight.touchcontroller.common.config.LayoutLayer
import top.fifthlight.touchcontroller.common.ui.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.common.ui.state.LayersTabState

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
        val layer = state.toLayer()
        screenModel.editPreset {
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
        screenModel.editPreset {
            copy(layout = ControllerLayout(layout.layers.add(layer)))
        }
    }

    fun moveLayer(index: Int, offset: Int) {
        screenModel.editPreset {
            val layer = layout.layers[index]
            val newIndex = (index + offset).coerceIn(layout.layers.indices)
            val newLayers = layout.layers.removeAt(index).add(newIndex, layer)
            copy(layout = ControllerLayout(newLayers))
        }
    }

    fun openCustomConditionDialog() {
        val currentScreenModel = screenModel.uiState.value as? CustomControlLayoutTabState.Enabled ?: return
        val currentPreset = currentScreenModel.selectedPreset ?: return
        _uiState.getAndUpdate {
            if (it is LayersTabState.Edit) {
                it.copy(
                    customConditionState = LayersTabState.Edit.CustomConditionState(
                        conditions = currentPreset.controlInfo.customConditions,
                    ),
                )
            } else {
                it
            }
        }
    }

    fun updateCustomConditionState(state: LayersTabState.Edit.CustomConditionState) {
        _uiState.getAndUpdate {
            if (it is LayersTabState.Edit) {
                it.copy(
                    customConditionState = state,
                )
            } else {
                it
            }
        }
    }

    fun applyCustomConditionState(state: LayersTabState.Edit.CustomConditionState) {
        screenModel.editPreset {
            copy(controlInfo = controlInfo.copy(customConditions = state.conditions))
        }
        _uiState.getAndUpdate {
            if (it is LayersTabState.Edit) {
                it.copy(customConditionState = null)
            } else {
                it
            }
        }
    }

    fun clearCustomConditionDialog() {
        _uiState.getAndUpdate {
            if (it is LayersTabState.Edit) {
                it.copy(customConditionState = null)
            } else {
                it
            }
        }
    }
}