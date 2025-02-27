package top.fifthlight.touchcontroller.ui.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import top.fifthlight.touchcontroller.ui.state.WidgetsTabState

class WidgetsTabModel(
    private val screenModel: CustomControlLayoutTabModel
) : TouchControllerScreenModel() {
    private val _uiState = MutableStateFlow(WidgetsTabState())
    val uiState = _uiState.asStateFlow()

    init {
        coroutineScope.launch {
            _uiState.collectLatest {
                if (it.listState is WidgetsTabState.ListState.Custom.Loading) {

                }
            }
        }
    }

    fun selectBuiltinTab() {
        _uiState.getAndUpdate {
            it.copy(listState = WidgetsTabState.ListState.Builtin)
        }
    }

    fun selectCustomTab() {
        _uiState.getAndUpdate {
            it.copy(listState = WidgetsTabState.ListState.Custom.Loading)
        }
    }

    fun openNewWidgetParamsDialog() {
        _uiState.getAndUpdate {
            it.copy(dialogState = WidgetsTabState.DialogState.ChangeNewWidgetParams(it.newWidgetParams))
        }
    }

    fun updateNewWidgetParamsDialog(editor: WidgetsTabState.DialogState.ChangeNewWidgetParams.() -> WidgetsTabState.DialogState.ChangeNewWidgetParams) {
        _uiState.getAndUpdate {
            var params = it.dialogState
            if (params is WidgetsTabState.DialogState.ChangeNewWidgetParams) {
                params = editor(params)
            }
            it.copy(dialogState = params)
        }
    }

    fun closeDialog() {
        _uiState.getAndUpdate {
            it.copy(dialogState = WidgetsTabState.DialogState.Empty)
        }
    }

    fun updateNewWidgetParams(params: WidgetsTabState.NewWidgetParams) {
        _uiState.getAndUpdate {
            it.copy(newWidgetParams = params)
        }
    }
}