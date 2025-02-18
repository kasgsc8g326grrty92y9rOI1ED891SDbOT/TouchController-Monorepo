package top.fifthlight.touchcontroller.ui.tab

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.Text

data class TabOptions(
    private val titleId: Identifier,
    val group: TabGroup? = null,
    val index: Int,
) {
    val title: Text
        @Composable
        get() = Text.translatable(titleId)
}

abstract class Tab : Screen {
    abstract val options: TabOptions
}
