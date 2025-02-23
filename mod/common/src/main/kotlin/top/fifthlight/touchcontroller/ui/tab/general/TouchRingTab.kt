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
import top.fifthlight.touchcontroller.config.GlobalConfigHolder
import top.fifthlight.touchcontroller.config.TouchRingConfig
import top.fifthlight.touchcontroller.ui.component.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.ui.component.SliderPreferenceItem
import top.fifthlight.touchcontroller.ui.tab.Tab
import top.fifthlight.touchcontroller.ui.tab.TabGroup
import top.fifthlight.touchcontroller.ui.tab.TabOptions

object TouchRingTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_TITLE,
        group = TabGroup.GeneralGroup,
        index = 2,
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
            fun update(editor: TouchRingConfig.() -> TouchRingConfig) {
                globalConfigHolder.saveConfig(globalConfig.let { config ->
                    config.copy(touchRing = editor(config.touchRing))
                })
            }
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_RADIUS_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_RADIUS_DESCRIPTION),
                range = 16..96,
                value = globalConfig.touchRing.radius,
                onValueChanged = { update { copy(radius = it) } }
            )
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_BORDER_WIDTH_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_BORDER_WIDTH_DESCRIPTION),
                range = 0..8,
                value = globalConfig.touchRing.outerRadius,
                onValueChanged = { update { copy(outerRadius = it) } },
            )
            SliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_INITIAL_PROGRESS_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_INITIAL_PROGRESS_DESCRIPTION),
                range = 0f..1f,
                value = globalConfig.touchRing.initialProgress,
                onValueChanged = { update { copy(initialProgress = it) } },
            )
        }
    }
}