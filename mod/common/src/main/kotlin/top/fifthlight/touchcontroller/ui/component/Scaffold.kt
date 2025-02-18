package top.fifthlight.touchcontroller.ui.component

import androidx.compose.runtime.Composable
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxHeight
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.widget.base.layout.Column
import top.fifthlight.combine.widget.base.layout.Row

@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    sideBar: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    Column(Modifier.fillMaxSize().then(modifier)) {
        topBar()
        Row(Modifier.weight(1f)) {
            sideBar()
            content(Modifier.weight(1f).fillMaxHeight())
        }
    }
}