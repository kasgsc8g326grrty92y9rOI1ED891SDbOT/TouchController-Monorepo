package top.fifthlight.touchcontroller.ui.model

import top.fifthlight.touchcontroller.config.preset.PresetConfig
import top.fifthlight.touchcontroller.config.preset.builtin.BuiltinPresetKey
import top.fifthlight.touchcontroller.ext.mapState

class ManageControlPresetsTabModel(
    private val configScreenModel: ConfigScreenModel
) : TouchControllerScreenModel() {
    val presetConfig = configScreenModel.uiState.mapState {
        when (val preset = it.config.preset) {
            is PresetConfig.BuiltIn -> preset
            is PresetConfig.Custom -> null
        }
    }

    fun update(config: PresetConfig.BuiltIn) {
        configScreenModel.updateConfig {
            copy(preset = config)
        }
    }

    fun updateKey(editor: BuiltinPresetKey.() -> BuiltinPresetKey) {
        configScreenModel.updateConfig {
            if (preset is PresetConfig.BuiltIn) {
                copy(preset = preset.copy(key = editor(preset.key)))
            } else {
                this
            }
        }
    }
}