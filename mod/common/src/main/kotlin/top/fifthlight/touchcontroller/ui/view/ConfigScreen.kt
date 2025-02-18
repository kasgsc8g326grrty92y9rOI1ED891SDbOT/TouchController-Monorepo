package top.fifthlight.touchcontroller.ui.view

import androidx.compose.runtime.Composable
import top.fifthlight.touchcontroller.ui.component.TouchControllerNavigator
import top.fifthlight.touchcontroller.ui.tab.AboutTab

@Composable
fun ConfigScreen() {
    TouchControllerNavigator(AboutTab)
}
