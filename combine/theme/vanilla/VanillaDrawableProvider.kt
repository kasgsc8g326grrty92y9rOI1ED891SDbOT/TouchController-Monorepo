package top.fifthlight.combine.theme.vanilla

import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.mergetools.api.ExpectFactory

interface VanillaDrawableProvider {
    val buttonDrawableSet: DrawableSet

    @ExpectFactory
    interface Factory {
        fun of(): VanillaDrawableProvider
    }
}
