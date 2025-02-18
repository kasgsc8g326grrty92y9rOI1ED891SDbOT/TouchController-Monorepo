package top.fifthlight.touchcontroller.ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxWidth
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.combine.widget.ui.TextButton
import top.fifthlight.touchcontroller.about.License
import top.fifthlight.touchcontroller.ui.component.AppBar
import top.fifthlight.touchcontroller.ui.component.Scaffold

class LicenseScreen(
    val license: License,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        TextButton(
                            onClick = { navigator?.pop() }
                        ) {
                            Text("< Back")
                        }
                    },
                    title = {
                        Text(license.name)
                    },
                )
            },
        ) { modifier ->
            license.content?.let { content ->
                Text(
                    text = content,
                    modifier = Modifier
                        .padding(4)
                        .verticalScroll()
                        .then(modifier)
                )
            }
        }
    }
}