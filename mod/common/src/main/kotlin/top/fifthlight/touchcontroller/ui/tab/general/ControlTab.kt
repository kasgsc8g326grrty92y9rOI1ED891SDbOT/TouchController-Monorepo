package top.fifthlight.touchcontroller.ui.tab.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.base.layout.Column
import top.fifthlight.touchcontroller.assets.BackgroundTextures
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.config.ControlConfig
import top.fifthlight.touchcontroller.config.GlobalConfigHolder
import top.fifthlight.touchcontroller.ui.component.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.ui.component.SliderPreferenceItem
import top.fifthlight.touchcontroller.ui.component.SwitchPreferenceItem
import top.fifthlight.touchcontroller.ui.tab.Tab
import top.fifthlight.touchcontroller.ui.tab.TabGroup
import top.fifthlight.touchcontroller.ui.tab.TabOptions

object ControlTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_GENERAL_CONTROL_TITLE,
        group = TabGroup.GeneralGroup,
        index = 1,
    )

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .padding(8)
                .verticalScroll()
                .background(BackgroundTextures.BRICK_BACKGROUND)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8),
        ) {
            val globalConfigHolder: GlobalConfigHolder = koinInject()
            val globalConfig by globalConfigHolder.config.collectAsState()
            fun update(editor: ControlConfig.() -> ControlConfig) {
                globalConfigHolder.saveConfig(globalConfig.let { config ->
                    config.copy(control = editor(config.control))
                })
            }
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_SPLIT_CONTROLS_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_SPLIT_CONTROLS_DESCRIPTION),
                value = globalConfig.control.splitControls,
                onValueChanged = { update { copy(splitControls = it) } }
            )
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_TOUCH_GESTURE_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_TOUCH_GESTURE_DESCRIPTION),
                value = globalConfig.control.disableTouchGesture,
                onValueChanged = { update { copy(disableTouchGesture = it) } }
            )
            SliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_MOVEMENT_SENSITIVITY_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_MOVEMENT_SENSITIVITY_DESCRIPTION),
                range = 0f..900f,
                value = globalConfig.control.viewMovementSensitivity,
                onValueChanged = { update { copy(viewMovementSensitivity = it) } }
            )
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_THRESHOLD_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_THRESHOLD_DESCRIPTION),
                range = 0..10,
                value = globalConfig.control.viewHoldDetectThreshold,
                onValueChanged = { update { copy(viewHoldDetectThreshold = it) } }
            )
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_TICKS_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_TICKS_DESCRIPTION),
                range = 1..60,
                value = globalConfig.control.viewHoldDetectTicks,
                onValueChanged = { update { copy(viewHoldDetectTicks = it) } }
            )
        }
    }
}