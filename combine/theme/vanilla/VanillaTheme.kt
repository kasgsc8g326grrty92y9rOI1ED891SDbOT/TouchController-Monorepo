package top.fifthlight.combine.theme.vanilla

import top.fifthlight.combine.theme.Theme

val VanillaTheme by lazy {
    val drawableProvider = VanillaDrawableProviderFactory.of()
    Theme(
        drawables = Theme.Drawables(
            button = drawableProvider.buttonDrawableSet,
        )
    )
}
