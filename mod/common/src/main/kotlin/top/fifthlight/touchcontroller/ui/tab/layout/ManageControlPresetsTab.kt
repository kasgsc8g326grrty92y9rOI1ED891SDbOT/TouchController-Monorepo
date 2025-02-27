package top.fifthlight.touchcontroller.ui.tab.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.placement.fillMaxWidth
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.widget.base.layout.Box
import top.fifthlight.combine.widget.base.layout.Column
import top.fifthlight.combine.widget.base.layout.Row
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.touchcontroller.assets.BackgroundTextures
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.config.preset.PresetConfig
import top.fifthlight.touchcontroller.ui.component.AppBar
import top.fifthlight.touchcontroller.ui.component.BackButton
import top.fifthlight.touchcontroller.ui.component.Scaffold
import top.fifthlight.touchcontroller.ui.model.ManageControlPresetsTabModel
import top.fifthlight.touchcontroller.ui.tab.Tab
import top.fifthlight.touchcontroller.ui.tab.TabGroup
import top.fifthlight.touchcontroller.ui.tab.TabOptions

object ManageControlPresetsTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_LAYOUT_MANAGE_CONTROL_PRESET,
        group = TabGroup.LayoutGroup,
        index = 0,
        openAsScreen = true,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_TITLE),
                        )
                    },
                )
            },
        ) { modifier ->
            val screenModel = koinScreenModel<ManageControlPresetsTabModel>()
            val presetConfig by screenModel.presetConfig.collectAsState()
            if (presetConfig != null) {
                Column(
                    modifier = Modifier
                        .background(BackgroundTextures.BRICK_BACKGROUND)
                        .then(modifier)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        alignment = Alignment.Center,
                    ) {
                        Text("Real-time preview")
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .padding(4)
                                .border(Textures.WIDGET_BACKGROUND_BACKGROUND_DARK)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = "Texture set",
                                )
                                Select(
                                    expanded = false,
                                    onExpandedChanged = {},
                                    dropDownContent = {},
                                    content = {
                                        Text("Classic")
                                        SelectIcon(false)
                                    }
                                )
                            }
                            Column {
                                Text("Opacity")
                                Slider(
                                    modifier = Modifier.fillMaxWidth(),
                                    range = 0f..1f,
                                    value = .6f,
                                    onValueChanged = {}
                                )
                            }
                            Column {
                                Text("Size")
                                Slider(
                                    modifier = Modifier.fillMaxWidth(),
                                    range = 0f..4f,
                                    value = 1f,
                                    onValueChanged = {}
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .border(Textures.WIDGET_BACKGROUND_BACKGROUND_DARK)
                                .weight(1f)
                        ) {

                        }
                    }
                }
            } else {
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
                        Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SWITCH_MESSAGE))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12),
                        ) {
                            WarningButton(
                                onClick = {
                                    screenModel.update(PresetConfig.BuiltIn())
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SWITCH_SWITCH))
                            }
                            GuideButton(
                                onClick = {
                                    navigator?.replace(CustomControlLayoutTab)
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SWITCH_GOTO_CUSTOM))
                            }
                        }
                    }
                }
            }
        }
    }
}