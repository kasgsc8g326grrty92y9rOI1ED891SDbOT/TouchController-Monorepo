package top.fifthlight.touchcontroller.ui.tab.layout

import androidx.compose.runtime.Composable
import org.koin.core.component.KoinComponent
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.widget.base.layout.Box
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.BackgroundTextures
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.ui.tab.Tab
import top.fifthlight.touchcontroller.ui.tab.TabGroup
import top.fifthlight.touchcontroller.ui.tab.TabOptions

object GuiControlLayoutTab : Tab(), KoinComponent {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_LAYOUT_GUI_CONTROL_LAYOUT_TITLE,
        group = TabGroup.LayoutGroup,
        index = 2,
    )

    @Composable
    override fun Content() {
        Box(
            modifier = Modifier
                .background(BackgroundTextures.BRICK_BACKGROUND)
                .fillMaxSize(),
            alignment = Alignment.Center,
        ) {
            Text("TODO")
        }
    }
}