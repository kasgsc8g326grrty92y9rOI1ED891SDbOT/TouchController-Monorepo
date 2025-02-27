package top.fifthlight.touchcontroller.ui.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import org.koin.core.component.inject
import top.fifthlight.touchcontroller.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.config.preset.PresetManager
import top.fifthlight.touchcontroller.ui.state.PresetsTabState
import kotlin.uuid.Uuid

class PresetsTabModel(
    private val screenModel: CustomControlLayoutTabModel
): TouchControllerScreenModel() {
    private val presetManager: PresetManager by inject()
    private val _uiState = MutableStateFlow<PresetsTabState>(PresetsTabState.Empty)
    val uiState = _uiState.asStateFlow()

    fun clearState() {
        _uiState.value = PresetsTabState.Empty
    }

    fun openCreatePresetDialog() {
        _uiState.value = PresetsTabState.Create()
    }

    fun updateCreatePresetState(editor: PresetsTabState.Create.() -> PresetsTabState.Create) {
        _uiState.getAndUpdate {
            if (it is PresetsTabState.Create) {
                editor(it)
            } else {
                it
            }
        }
    }

    fun createPreset(state: PresetsTabState.Create) {
        val preset = state.toPreset()
        screenModel.newPreset(preset)
        clearState()
    }

    fun openEditPresetDialog(uuid: Uuid, preset: LayoutPreset) {
        _uiState.value = PresetsTabState.Edit(
            uuid = uuid,
            name = preset.name,
            controlInfo = preset.controlInfo,
        )
    }

    fun updateEditPresetState(editor: PresetsTabState.Edit.() -> PresetsTabState.Edit) {
        _uiState.getAndUpdate {
            if (it is PresetsTabState.Edit) {
                editor(it)
            } else {
                it
            }
        }
    }

    fun editPreset(state: PresetsTabState.Edit) {
        screenModel.editPreset(state.uuid, state::edit)
        clearState()
    }

    fun openDeletePresetBox(uuid: Uuid) {
        _uiState.value = PresetsTabState.Delete(uuid)
    }

    fun movePreset(uuid: Uuid, offset: Int) {
        presetManager.movePreset(uuid, offset)
    }
}