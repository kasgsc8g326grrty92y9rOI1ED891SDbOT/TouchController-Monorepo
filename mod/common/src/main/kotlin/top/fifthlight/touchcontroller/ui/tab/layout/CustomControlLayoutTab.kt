package top.fifthlight.touchcontroller.ui.tab.layout

import androidx.compose.runtime.*
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.collections.immutable.PersistentList
import org.koin.core.component.KoinComponent
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.layout.Layout
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.ParentDataModifierNode
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.drawing.innerLine
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.modifier.pointer.consumePress
import top.fifthlight.combine.modifier.pointer.draggable
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.widget.base.layout.Box
import top.fifthlight.combine.widget.base.layout.Column
import top.fifthlight.combine.widget.base.layout.Row
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.assets.BackgroundTextures
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.config.LayoutLayer
import top.fifthlight.touchcontroller.control.ControllerWidget
import top.fifthlight.touchcontroller.layout.Align
import top.fifthlight.touchcontroller.ui.component.*
import top.fifthlight.touchcontroller.ui.model.CustomControlLayoutTabModel
import top.fifthlight.touchcontroller.ui.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.ui.tab.Tab
import top.fifthlight.touchcontroller.ui.tab.TabGroup
import top.fifthlight.touchcontroller.ui.tab.TabOptions
import top.fifthlight.touchcontroller.ui.tab.layout.custom.CustomTab
import top.fifthlight.touchcontroller.ui.tab.layout.custom.CustomTabContext
import top.fifthlight.touchcontroller.ui.tab.layout.custom.LocalCustomTabContext
import top.fifthlight.touchcontroller.ui.tab.layout.custom.allCustomTabs

private data class ControllerWidgetParentData(
    val align: Align,
    val offset: IntOffset,
    val size: IntSize,
)

private data class ControllerWidgetModifierNode(
    val align: Align,
    val offset: IntOffset,
    val size: IntSize,
) : ParentDataModifierNode, Modifier.Node<ControllerWidgetModifierNode> {
    constructor(widget: ControllerWidget) : this(widget.align, widget.offset, widget.size())

    override fun modifierParentData(parentData: Any?): ControllerWidgetParentData {
        return ControllerWidgetParentData(
            align = align,
            offset = offset,
            size = size
        )
    }
}

@Composable
private fun LayoutEditorPanel(
    modifier: Modifier = Modifier,
    selectedWidgetIndex: Int = -1,
    onSelectedWidgetChanged: (Int) -> Unit = { _ -> },
    layer: LayoutLayer,
    layerIndex: Int,
    lockMoving: Boolean = false,
    onWidgetChanged: (Int, ControllerWidget) -> Unit = { _, _ -> },
) {
    val selectedWidget = layer.widgets.getOrNull(selectedWidgetIndex)
    var panelSize by remember { mutableStateOf(IntSize.ZERO) }
    Layout(
        modifier = Modifier
            .consumePress {
                onSelectedWidgetChanged(-1)
            }
            .then(modifier),
        measurePolicy = { measurables, constraints ->
            val childConstraint = constraints.copy(minWidth = 0, minHeight = 0)
            val placeables = measurables.map { it.measure(childConstraint) }

            val width = constraints.maxWidth
            val height = constraints.maxHeight
            panelSize = IntSize(width, height)
            layout(width, height) {
                placeables.forEachIndexed { index, placeable ->
                    val parentData = measurables[index].parentData as ControllerWidgetParentData
                    placeable.placeAt(
                        parentData.align.alignOffset(
                            windowSize = IntSize(width, height),
                            size = parentData.size,
                            offset = parentData.offset
                        )
                    )
                }
            }
        }
    ) {
        for ((index, widget) in layer.widgets.withIndex()) {
            if (selectedWidgetIndex == index) {
                var dragTotalOffset by remember { mutableStateOf(Offset.ZERO) }
                var widgetInitialOffset by remember { mutableStateOf(IntOffset.ZERO) }
                LaunchedEffect(selectedWidgetIndex, layerIndex, selectedWidget?.align) {
                    widgetInitialOffset = layer.widgets.getOrNull(selectedWidgetIndex)?.offset ?: IntOffset.ZERO
                    dragTotalOffset = Offset.ZERO
                }
                val lockWidgetMoving = lockMoving || widget.lockMoving
                val dragModifier = if (!lockWidgetMoving) {
                    Modifier.innerLine(Colors.WHITE)
                        .draggable { offset ->
                            dragTotalOffset += offset
                            val intOffset = dragTotalOffset.toIntOffset()
                            val appliedOffset = when (widget.align) {
                                Align.LEFT_TOP, Align.CENTER_TOP, Align.LEFT_CENTER, Align.CENTER_CENTER -> intOffset
                                Align.RIGHT_TOP, Align.RIGHT_CENTER -> IntOffset(-intOffset.x, intOffset.y)
                                Align.LEFT_BOTTOM, Align.CENTER_BOTTOM -> IntOffset(intOffset.x, -intOffset.y)
                                Align.RIGHT_BOTTOM -> -intOffset
                            }
                            val widgetOffset = widgetInitialOffset + appliedOffset
                            val widgetSize = widget.size()
                            val clampedOffset = IntOffset(
                                x = when (widget.align) {
                                    Align.LEFT_TOP, Align.LEFT_CENTER, Align.LEFT_BOTTOM ->
                                        widgetOffset.x.coerceIn(0, panelSize.width - widgetSize.width)

                                    Align.CENTER_CENTER, Align.CENTER_BOTTOM, Align.CENTER_TOP ->
                                        widgetOffset.x.coerceIn(
                                            -panelSize.width / 2 + widgetSize.width / 2,
                                            panelSize.width / 2 - widgetSize.width / 2
                                        )

                                    Align.RIGHT_TOP, Align.RIGHT_CENTER, Align.RIGHT_BOTTOM ->
                                        widgetOffset.x.coerceIn(0, panelSize.width - widgetSize.width)
                                },
                                y = when (widget.align) {
                                    Align.LEFT_TOP, Align.CENTER_TOP, Align.RIGHT_TOP ->
                                        widgetOffset.y.coerceIn(0, panelSize.height - widgetSize.height)

                                    Align.LEFT_CENTER, Align.CENTER_CENTER, Align.RIGHT_CENTER ->
                                        widgetOffset.y.coerceIn(
                                            -panelSize.height / 2 + widgetSize.height / 2,
                                            panelSize.height / 2 - widgetSize.height / 2
                                        )

                                    Align.LEFT_BOTTOM, Align.CENTER_BOTTOM, Align.RIGHT_BOTTOM ->
                                        widgetOffset.y.coerceIn(0, panelSize.height - widgetSize.height)
                                }
                            )
                            val newWidget = widget.cloneBase(
                                offset = clampedOffset,
                            )
                            onWidgetChanged(index, newWidget)
                        }
                } else {
                    Modifier
                        .innerLine(Colors.RED)
                        .consumePress()
                }
                ControllerWidget(
                    modifier = Modifier
                        .then(ControllerWidgetModifierNode(widget))
                        .then(dragModifier),
                    widget = widget
                )
            } else {
                ControllerWidget(
                    modifier = Modifier
                        .then(ControllerWidgetModifierNode(widget))
                        .clickable {
                            onSelectedWidgetChanged(index)
                        },
                    widget = widget
                )
            }
        }
    }
}

@Composable
private fun SideBar(
    onTabSelected: (CustomTab) -> Unit,
    allTabs: PersistentList<CustomTab>,
    selectedTab: CustomTab? = null,
) {
    for (tab in allTabs) {
        IconButton(
            selected = selectedTab == tab,
            onClick = {
                onTabSelected(tab)
            },
        ) {
            tab.Icon()
        }
    }
}

object CustomControlLayoutTab : Tab(), KoinComponent {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_LAYOUT_CUSTOM_CONTROL_LAYOUT,
        group = TabGroup.LayoutGroup,
        index = 1,
        openAsScreen = true,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel: CustomControlLayoutTabModel = koinScreenModel()
        val currentUiState by screenModel.uiState.collectAsState()
        val uiState = currentUiState
        if (uiState is CustomControlLayoutTabState.Enabled) {
            Scaffold(
                topBar = {
                    AppBar(
                        modifier = Modifier.fillMaxWidth(),
                        leading = {
                            BackButton(
                                screenName = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_TITLE),
                            )

                            val copiedWidget = uiState.pageState.copiedWidget
                            Button(
                                onClick = {
                                    copiedWidget?.let(screenModel::newWidget)
                                },
                                enabled = copiedWidget != null
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PASTE_WIDGET))
                            }

                            val widgetIndex = uiState.selectedLayer?.widgets?.indices?.let { indices ->
                                uiState.pageState.selectedWidgetIndex.takeIf { it in indices }
                            }
                            WarningButton(
                                onClick = {
                                    widgetIndex?.let(screenModel::deleteWidget)
                                },
                                enabled = widgetIndex != null
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_DELETE_WIDGET))
                            }
                        },
                        trailing = {
                            CheckBoxButton(
                                checked = uiState.pageState.showSideBar,
                                onClick = {
                                    screenModel.setShowSideBar(!uiState.pageState.showSideBar)
                                },
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SHOW_SIDE_BAR))
                            }
                        }
                    )
                },
            ) { modifier ->
                var anchor by remember { mutableStateOf(IntRect.ZERO) }
                Box(
                    modifier = Modifier
                        .background(BackgroundTextures.BRICK_BACKGROUND)
                        .anchor { anchor = it }
                        .then(modifier),
                    alignment = Alignment.Center,
                ) {
                    if (uiState.selectedLayer != null) {
                        LayoutEditorPanel(
                            modifier = Modifier.fillMaxSize(),
                            selectedWidgetIndex = uiState.pageState.selectedWidgetIndex,
                            onSelectedWidgetChanged = screenModel::selectWidget,
                            layer = uiState.selectedLayer,
                            layerIndex = uiState.pageState.selectedLayerIndex,
                            lockMoving = uiState.pageState.moveLocked,
                            onWidgetChanged = screenModel::editWidget,
                        )
                    } else if (uiState.selectedPreset != null) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_NO_LAYER_SELECTED))
                    } else {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_NO_PRESET_SELECTED))
                    }

                    if (uiState.pageState.showSideBar) {
                        TouchControllerNavigator(allCustomTabs.first()) { navigator ->
                            val currentScreen = navigator.lastItem

                            @Composable
                            fun SideBar() = SideBar(
                                allTabs = allCustomTabs,
                                onTabSelected = navigator::replace,
                                selectedTab = currentScreen as? CustomTab,
                            )

                            val sideBarAtRight by remember(uiState.selectedWidget, anchor) {
                                derivedStateOf {
                                    uiState.selectedWidget?.let { widget ->
                                        val editAreaSize = anchor.size
                                        val widgetSize = widget.size()
                                        val offset =
                                            widget.align.alignOffset(editAreaSize, widget.size(), widget.offset)
                                        val centerOffset = offset + widgetSize / 2
                                        centerOffset.left < editAreaSize.width / 2
                                    } != false
                                }
                            }

                            val currentCustomTabContext = CustomTabContext(
                                screenModel = screenModel,
                                uiState = uiState,
                                tabsButton = @Composable { SideBar() },
                                sideBarAtRight = sideBarAtRight,
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(.4f)
                                    .fillMaxHeight()
                                    .alignment(
                                        if (sideBarAtRight) {
                                            Alignment.CenterRight
                                        } else {
                                            Alignment.CenterLeft
                                        }
                                    )
                                    .consumePress(),
                            ) {
                                CompositionLocalProvider(
                                    LocalCustomTabContext provides currentCustomTabContext,
                                ) {
                                    CurrentScreen()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Scaffold(
                topBar = {
                    AppBar(
                        modifier = Modifier.fillMaxWidth(),
                        leading = {
                            BackButton(
                                screenName = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_TITLE),
                            )
                        },
                    )
                },
            ) { modifier ->
                Box(
                    modifier = Modifier
                        .background(BackgroundTextures.BRICK_BACKGROUND)
                        .then(modifier),
                    alignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12)
                            .border(Textures.WIDGET_BACKGROUND_BACKGROUND_DARK),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12),
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SWITCH_MESSAGE))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12),
                        ) {
                            WarningButton(
                                onClick = {
                                    screenModel.enableCustomLayout()
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SWITCH_SWITCH))
                            }
                            GuideButton(
                                onClick = {
                                    navigator?.replace(ManageControlPresetsTab)
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SWITCH_GOTO_PRESET))
                            }
                        }
                    }
                }
            }
        }
    }
}
