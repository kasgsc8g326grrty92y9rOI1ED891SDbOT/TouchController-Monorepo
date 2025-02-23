package top.fifthlight.touchcontroller.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.ui.style.ColorTheme
import top.fifthlight.combine.ui.style.NinePatchTextureSet
import top.fifthlight.combine.widget.base.layout.BoxScope
import top.fifthlight.combine.widget.ui.Button
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.Textures

data class CheckButtonTextures(
    val unchecked: NinePatchTextureSet,
    val checked: NinePatchTextureSet,
)

data class CheckButtonColors(
    val unchecked: ColorTheme,
    val checked: ColorTheme,
)

val checkButtonColors = CheckButtonColors(
    unchecked = ColorTheme.dark,
    checked = ColorTheme.light,
)

val tabButtonTexture = CheckButtonTextures(
    unchecked = NinePatchTextureSet(
        normal = Textures.WIDGET_TAB_TAB,
        focus = Textures.WIDGET_TAB_TAB_HOVER,
        hover = Textures.WIDGET_TAB_TAB_HOVER,
        active = Textures.WIDGET_TAB_TAB_ACTIVE,
        disabled = Textures.WIDGET_TAB_TAB_DISABLED,
    ),
    checked = NinePatchTextureSet(
        normal = Textures.WIDGET_TAB_TAB_PRESSLOCK,
        focus = Textures.WIDGET_TAB_TAB_PRESSLOCK_HOVER,
        hover = Textures.WIDGET_TAB_TAB_PRESSLOCK_HOVER,
        active = Textures.WIDGET_TAB_TAB_ACTIVE,
        disabled = Textures.WIDGET_TAB_TAB_DISABLED,
    ),
)

val listButtonTexture = CheckButtonTextures(
    unchecked = NinePatchTextureSet(
        normal = Textures.WIDGET_LIST_LIST,
        focus = Textures.WIDGET_LIST_LIST_HOVER,
        hover = Textures.WIDGET_LIST_LIST_HOVER,
        active = Textures.WIDGET_LIST_LIST_ACTIVE,
        disabled = Textures.WIDGET_LIST_LIST_DISABLED,
    ),
    checked = NinePatchTextureSet(
        normal = Textures.WIDGET_LIST_LIST_PRESSLOCK,
        focus = Textures.WIDGET_LIST_LIST_PRESSLOCK_HOVER,
        hover = Textures.WIDGET_LIST_LIST_PRESSLOCK_HOVER,
        active = Textures.WIDGET_LIST_LIST_ACTIVE,
        disabled = Textures.WIDGET_LIST_LIST_DISABLED,
    ),
)

val LocalTabButtonTexture = staticCompositionLocalOf { tabButtonTexture }
val LocalListButtonTexture = staticCompositionLocalOf { listButtonTexture }
val LocalCheckButtonColors = staticCompositionLocalOf { checkButtonColors }

@Composable
fun TabButton(
    modifier: Modifier = Modifier,
    textures: CheckButtonTextures = LocalTabButtonTexture.current,
    colors: CheckButtonColors = LocalCheckButtonColors.current,
    checked: Boolean = false,
    minSize: IntSize = IntSize(48, 20),
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) = CheckButton(
    modifier = modifier,
    textures = textures,
    colors = colors,
    checked = checked,
    minSize = minSize,
    onClick = onClick,
    clickSound = clickSound,
    content = content,
)

@Composable
fun ListButton(
    modifier: Modifier = Modifier,
    textures: CheckButtonTextures = LocalListButtonTexture.current,
    colors: CheckButtonColors = LocalCheckButtonColors.current,
    checked: Boolean = false,
    minSize: IntSize = IntSize(48, 20),
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) = CheckButton(
    modifier = modifier,
    textures = textures,
    colors = colors,
    checked = checked,
    minSize = minSize,
    onClick = onClick,
    clickSound = clickSound,
    content = content,
)

@Composable
fun CheckButton(
    modifier: Modifier = Modifier,
    textures: CheckButtonTextures,
    colors: CheckButtonColors = LocalCheckButtonColors.current,
    checked: Boolean = false,
    minSize: IntSize = IntSize(48, 20),
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Button(
        modifier = modifier,
        textureSet = if (checked) {
            textures.checked
        } else {
            textures.unchecked
        },
        colorTheme = if (checked) {
            colors.checked
        } else {
            colors.unchecked
        },
        minSize = minSize,
        onClick = onClick,
        clickSound = clickSound,
        content = content,
    )
}