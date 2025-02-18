package top.fifthlight.touchcontroller.ui.screen

import org.koin.core.context.GlobalContext
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.combine.screen.ScreenFactory
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.ui.view.ConfigScreen

fun getConfigScreenButtonText(): Any = with(GlobalContext.get()) {
    val textFactory: TextFactory = get()
    textFactory.of(Texts.SCREEN_OPTIONS)
}

fun getConfigScreen(parent: Any?): Any? = with(GlobalContext.get()) {
    val textFactory: TextFactory = get()
    val screenFactory: ScreenFactory = get()
    screenFactory.getScreen(parent, textFactory.of(Texts.SCREEN_OPTIONS_TITLE)) {
        ConfigScreen()
    }
}
