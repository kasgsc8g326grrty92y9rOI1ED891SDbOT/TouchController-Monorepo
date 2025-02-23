package top.fifthlight.touchcontroller.ui.screen

import cafe.adriel.voyager.navigator.CurrentScreen
import org.koin.core.context.GlobalContext
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxHeight
import top.fifthlight.combine.modifier.placement.fillMaxWidth
import top.fifthlight.combine.screen.ScreenFactory
import top.fifthlight.combine.widget.base.layout.Box
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.ui.component.*
import top.fifthlight.touchcontroller.ui.tab.AboutTab
import top.fifthlight.touchcontroller.ui.tab.Tab

fun getConfigScreenButtonText(): Any = with(GlobalContext.get()) {
    val textFactory: TextFactory = get()
    textFactory.of(Texts.SCREEN_CONFIG)
}

fun getConfigScreen(parent: Any?): Any? = with(GlobalContext.get()) {
    val textFactory: TextFactory = get()
    val screenFactory: ScreenFactory = get()
    screenFactory.getScreen(
        parent = parent,
        renderBackground = false,
        title = textFactory.of(Texts.SCREEN_CONFIG_TITLE)
    ) {
        TouchControllerNavigator(AboutTab) { navigator ->
            val currentTab = navigator.lastItem as? Tab
            currentTab?.let {
                Scaffold(
                    topBar = {
                        AppBar(
                            modifier = Modifier.fillMaxWidth(),
                            leading = {
                                BackButton(
                                    screenName = Text.translatable(Texts.SCREEN_CONFIG_TITLE),
                                    close = true
                                )
                            },
                            title = {
                                Text(currentTab.options.title)
                            },
                        )
                    },
                    sideBar = {
                        SideTabBar(
                            modifier = Modifier.fillMaxHeight(),
                            onTabSelected = {
                                navigator.replace(it)
                            }
                        )
                    },
                ) { modifier ->
                    Box(modifier) {
                        CurrentScreen()
                    }
                }
            } ?: run {
                CurrentScreen()
            }
        }
    }
}
