package top.fifthlight.touchcontroller.common.ui.tab.layout.custom

import androidx.compose.runtime.*
import cafe.adriel.voyager.koin.koinScreenModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import org.koin.core.parameter.parametersOf
import top.fifthlight.combine.data.LocalTextFactory
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.base.layout.*
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.config.LayerConditionKey
import top.fifthlight.touchcontroller.common.config.LayerConditionValue
import top.fifthlight.touchcontroller.common.config.preset.CustomCondition
import top.fifthlight.touchcontroller.common.config.preset.CustomConditions
import top.fifthlight.touchcontroller.common.config.text
import top.fifthlight.touchcontroller.common.ui.component.ListButton
import top.fifthlight.touchcontroller.common.ui.model.LayersTabModel
import top.fifthlight.touchcontroller.common.ui.state.LayersTabState
import kotlin.uuid.Uuid

private data class LayerCondition(
    val preset: PersistentMap<LayerConditionKey, LayerConditionValue>,
    val custom: PersistentMap<Uuid, LayerConditionValue>,
)

@Composable
private fun LayerConditionPanel(
    modifier: Modifier = Modifier,
    value: LayerCondition,
    customConditions: PersistentList<CustomCondition>,
    onValueChanged: (LayerCondition) -> Unit = {},
) {
    FlowRow(
        modifier = modifier,
        maxColumns = 2,
        horizontalSpacing = 4,
        expandColumnWidth = true,
    ) {
        val (preset, custom) = value
        @Composable
        fun Item(
            label: Text,
            value: LayerConditionValue?,
            onValueChanged: (LayerConditionValue?) -> Unit,
        ) {
            var expanded by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = label,
                )
                Select(
                    modifier = Modifier.weight(1f),
                    expanded = expanded,
                    onExpandedChanged = { expanded = it },
                    dropDownContent = {
                        val selectedIndex = LayerConditionValue.allValues.indexOf(value)
                        val textFactory = LocalTextFactory.current
                        DropdownItemList(
                            modifier = Modifier.verticalScroll(),
                            items = LayerConditionValue.allValues,
                            textProvider = { textFactory.of(it.text()) },
                            selectedIndex = selectedIndex,
                            onItemSelected = { index ->
                                expanded = false
                                onValueChanged(LayerConditionValue.allValues[index])
                            }
                        )
                    }
                ) {
                    Text(Text.translatable(value.text()))
                    Spacer(Modifier.weight(1f))
                    SelectIcon(expanded = expanded)
                }
            }
        }
        for (key in LayerConditionKey.entries) {
            Item(
                label = Text.translatable(key.text),
                value = preset[key],
                onValueChanged = {
                    onValueChanged(
                        value.copy(
                            preset = when (val conditionValue = it) {
                                null -> preset.remove(key)
                                else -> preset.put(key, conditionValue)
                            }
                        )
                    )
                }
            )
        }
        for (condition in customConditions) {
            Item(
                label = condition.name?.let { Text.literal(it) }
                    ?: Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_UNNAMED),
                value = custom[condition.uuid],
                onValueChanged = {
                    onValueChanged(
                        value.copy(
                            custom = when (val conditionValue = it) {
                                null -> custom.remove(condition.uuid)
                                else -> custom.put(condition.uuid, conditionValue)
                            }
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun CustomConditionDialog(
    state: LayersTabState.Edit.CustomConditionState?,
    onUpdate: (LayersTabState.Edit.CustomConditionState) -> Unit,
    onFinish: (LayersTabState.Edit.CustomConditionState) -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        value = state,
        valueTransformer = { it },
        modifier = Modifier
            .fillMaxWidth(.5f)
            .fillMaxHeight(.7f),
        title = {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION))
        },
        action = {
            Button(
                onClick = {
                    val state = state ?: return@Button
                    onUpdate(
                        state.copy(
                            conditions = CustomConditions(
                                state.conditions.conditions + CustomCondition()
                            )
                        )
                    )
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_ADD))
            }
            Spacer(Modifier.weight(1f))
            GuideButton(
                onClick = {
                    val state = state ?: return@GuideButton
                    onFinish(state)
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_OK))
            }
            Button(
                onClick = {
                    onCancel()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_CANCEL))
            }
        },
    ) { state ->
        Column(
            modifier = Modifier
                .verticalScroll()
                .weight(1f)
                .fillMaxWidth()
        ) {
            for ((index, condition) in state.conditions.conditions.withIndex()) {
                Row(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .border(Textures.WIDGET_LIST_LIST)
                            .weight(1f)
                            .minHeight(22),
                        alignment = Alignment.CenterLeft,
                    ) {
                        Text(
                            modifier = Modifier.padding(top = 1, left = 2),
                            text = condition.name?.let { Text.literal(it) }
                                ?: Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_UNNAMED),
                        )
                    }

                    IconButton(
                        onClick = {
                            onUpdate(
                                state.copy(
                                    editData = LayersTabState.Edit.CustomConditionState.EditData(
                                        index = index,
                                        condition = condition,
                                    )
                                )
                            )
                        }
                    ) {
                        Icon(Textures.ICON_EDIT)
                    }
                    IconButton(
                        onClick = {
                            onUpdate(
                                state.copy(
                                    conditions = CustomConditions(
                                        state.conditions.conditions.removeAt(index)
                                    ),
                                )
                            )
                        }
                    ) {
                        Icon(Textures.ICON_DELETE)
                    }
                }
            }
        }
        AlertDialog(
            value = state.editData,
            valueTransformer = { it },
            title = {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_EDIT))
            },
            action = { editState ->
                GuideButton(
                    onClick = {
                        onUpdate(
                            state.copy(
                                conditions = CustomConditions(
                                    state.conditions.conditions.set(editState.index, editState.condition)
                                ),
                                editData = null,
                            )
                        )
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_EDIT_OK))
                }
                Button(
                    onClick = {
                        onUpdate(state.copy(editData = null))
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_EDIT_CANCEL))
                }
            }
        ) { editData ->
            val condition = editData.condition
            val defaultString =
                Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_UNNAMED).string
            EditText(
                modifier = Modifier.fillMaxWidth(.4f),
                value = condition.name ?: defaultString,
                onValueChanged = {
                    onUpdate(
                        state.copy(
                            editData = editData.copy(
                                condition = condition.copy(name = it)
                            )
                        )
                    )
                },
                placeholder = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CUSTOM_CONDITION_EDIT_NAME_PLACEHOLDER),
            )
        }
    }
}

object LayersTab : CustomTab() {
    @Composable
    override fun Icon() {
        Icon(Textures.ICON_LAYER)
    }

    @Composable
    override fun Content() {
        val (screenModel, uiState, tabsButton, sideBarAtRight) = LocalCustomTabContext.current
        val tabModel: LayersTabModel = koinScreenModel { parametersOf(screenModel) }
        val tabState by tabModel.uiState.collectAsState()
        val customConditions = uiState.selectedPreset?.controlInfo?.customConditions?.conditions ?: persistentListOf()
        AlertDialog(
            value = tabState,
            valueTransformer = { tabState as? LayersTabState.Create },
            title = {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CREATE_LAYER))
            },
            action = { state ->
                GuideButton(
                    onClick = {
                        tabModel.createLayer(state)
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CREATE_LAYER_CREATE))
                }
                Button(
                    onClick = {
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CREATE_LAYER_CANCEL))
                }
            }
        ) { state ->
            Column(
                modifier = Modifier
                    .fillMaxWidth(.5f)
                    .fillMaxHeight(.8f),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                EditText(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.name,
                    onValueChanged = {
                        tabModel.updateCreateLayerState { copy(name = it) }
                    },
                    placeholder = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_NAME_PLACEHOLDER),
                )

                LayerConditionPanel(
                    modifier = Modifier
                        .verticalScroll()
                        .weight(1f)
                        .fillMaxWidth(),
                    value = LayerCondition(
                        preset = state.condition,
                        custom = state.customConditions,
                    ),
                    customConditions = customConditions,
                    onValueChanged = {
                        tabModel.updateCreateLayerState {
                            copy(
                                condition = it.preset,
                                customConditions = it.custom,
                            )
                        }
                    },
                )
            }
        }
        AlertDialog(
            value = tabState,
            valueTransformer = { tabState as? LayersTabState.Edit },
            title = {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_EDIT_LAYER))
            },
            action = { state ->
                GuideButton(
                    onClick = {
                        tabModel.clearState()
                        screenModel.editLayer(state::edit)
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_EDIT_LAYER_OK))
                }
                Button(
                    onClick = {
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_EDIT_LAYER_CANCEL))
                }
            }
        ) { state ->
            Column(
                modifier = Modifier
                    .fillMaxWidth(.5f)
                    .fillMaxHeight(.8f),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                EditText(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.name,
                    onValueChanged = {
                        tabModel.updateEditLayerState { copy(name = it) }
                    },
                    placeholder = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_NAME_PLACEHOLDER),
                )

                LayerConditionPanel(
                    modifier = Modifier
                        .verticalScroll()
                        .weight(1f)
                        .fillMaxWidth(),
                    value = LayerCondition(
                        preset = state.condition,
                        custom = state.customConditions,
                    ),
                    customConditions = customConditions,
                    onValueChanged = {
                        tabModel.updateEditLayerState {
                            copy(
                                condition = it.preset,
                                customConditions = it.custom,
                            )
                        }
                    },
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        tabModel.openCustomConditionDialog()
                    }
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_EDIT_LAYER_CUSTOM_CONDITION))
                }

                CustomConditionDialog(
                    state = state.customConditionState,
                    onUpdate = {
                        tabModel.updateCustomConditionState(it)
                    },
                    onFinish = {
                        tabModel.applyCustomConditionState(it)
                    },
                    onCancel = {
                        tabModel.clearCustomConditionDialog()
                    }
                )
            }
        }
        AlertDialog(
            value = tabState,
            valueTransformer = { tabState as? LayersTabState.Delete },
            onDismissRequest = { tabModel.clearState() },
            action = { state ->
                WarningButton(
                    onClick = {
                        screenModel.deleteLayer(state.index)
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_DELETE))
                }
                Button(
                    onClick = {
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_CANCEL))
                }
            }
        ) { state ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_1))
                Text(Text.format(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_2, state.layer.name))
            }
        }
        SideBarContainer(
            sideBarAtRight = sideBarAtRight,
            tabsButton = tabsButton,
            actions = {
                val currentLayer = uiState.selectedLayer
                IconButton(
                    onClick = {
                        tabModel.openCreateLayerDialog()
                    },
                    enabled = uiState.selectedPreset != null,
                ) {
                    Icon(Textures.ICON_ADD)
                }
                IconButton(
                    onClick = {
                        currentLayer?.let(tabModel::copyLayer)
                    },
                    enabled = currentLayer != null,
                ) {
                    Icon(Textures.ICON_COPY)
                }
                IconButton(
                    onClick = {
                        val index = uiState.pageState.selectedLayerIndex
                        val layer = currentLayer ?: return@IconButton
                        tabModel.openEditLayerDialog(index, layer)
                    },
                    enabled = currentLayer != null,
                ) {
                    Icon(Textures.ICON_CONFIG)
                }
                IconButton(
                    onClick = {
                        val index = uiState.pageState.selectedLayerIndex
                        val layer = currentLayer ?: return@IconButton
                        tabModel.openDeleteLayerDialog(index, layer)
                    },
                    enabled = currentLayer != null,
                ) {
                    Icon(Textures.ICON_DELETE)
                }
            }
        ) { modifier ->
            SideBarScaffold(
                modifier = modifier,
                title = {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS))
                },
                actions = if (uiState.selectedPreset != null) {
                    {
                        val selectedPreset = uiState.selectedPreset
                        val layerIndices = selectedPreset.layout.indices
                        val selectedLayerIndex = uiState.pageState.selectedLayerIndex.takeIf { it in layerIndices }
                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = selectedLayerIndex != null && selectedLayerIndex > 0,
                            onClick = {
                                selectedLayerIndex?.let { index ->
                                    tabModel.moveLayer(index, -1)
                                    screenModel.selectLayer(index - 1)
                                }
                            }
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_MOVE_UP))
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = selectedLayerIndex != null && selectedLayerIndex < layerIndices.last,
                            onClick = {
                                selectedLayerIndex?.let { index ->
                                    tabModel.moveLayer(index, 1)
                                    screenModel.selectLayer(index + 1)
                                }
                            }
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_MOVE_DOWN))
                        }
                    }
                } else {
                    null
                }
            ) {
                if (uiState.selectedPreset != null) {
                    Column(
                        modifier = Modifier
                            .verticalScroll()
                            .fillMaxSize()
                    ) {
                        for ((index, layer) in uiState.selectedPreset.layout.withIndex()) {
                            ListButton(
                                checked = index == uiState.pageState.selectedLayerIndex,
                                onClick = {
                                    screenModel.selectLayer(index)
                                },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = layer.name,
                                    )
                                    Text(
                                        Text.format(
                                            Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_COUNT,
                                            layer.condition.conditions.size
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        alignment = Alignment.Center,
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_NO_PRESET_SELECTED))
                    }
                }
            }
        }
    }
}