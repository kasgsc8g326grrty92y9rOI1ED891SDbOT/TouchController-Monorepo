package top.fifthlight.touchcontroller.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import top.fifthlight.combine.screen.LocalOnDismissRequestDispatcher
import top.fifthlight.combine.screen.OnDismissHandler

@Composable
fun DismissHandler(enabled: Boolean = true, handler: () -> Unit) {
    val handler = remember(enabled, handler) {
        object : OnDismissHandler {
            override val isEnabled: Boolean = enabled

            override fun handleOnDismissed() {
                handler()
            }
        }
    }

    val dispatcher = LocalOnDismissRequestDispatcher.current
    DisposableEffect(dispatcher, handler) {
        dispatcher.addHandler(handler)
        onDispose {
            dispatcher.removeHandler(handler)
        }
    }
}