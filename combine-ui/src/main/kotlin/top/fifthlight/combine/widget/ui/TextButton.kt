package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.*
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.focus.focusable
import top.fifthlight.combine.modifier.placement.minSize
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.sound.LocalSoundManager
import top.fifthlight.combine.sound.SoundKind
import top.fifthlight.combine.ui.style.*
import top.fifthlight.combine.widget.base.layout.Box
import top.fifthlight.combine.widget.base.layout.BoxScope

val defaultTextButtonColorSet = ColorSet(
    normal = Color(0xFFC6C6C6u),
    focus = Color(0xFF717171u),
    hover = Color(0xFF717171u),
    active = Color(0xFF228207u),
    disabled = Color(0xFF323335u),
)

val LocalTextButtonColorSet = staticCompositionLocalOf<ColorSet> { defaultTextButtonColorSet }

@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    colorSet: ColorSet = LocalTextButtonColorSet.current,
    colorTheme: ColorTheme? = null,
    textStyle: TextStyle? = null,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val soundManager = LocalSoundManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val state by widgetState(interactionSource)
    val color = colorSet.getByState(state)

    Box(
        modifier = Modifier
            .background(color)
            .minSize(48, 20)
            .clickable(interactionSource) {
                if (clickSound) {
                    soundManager.play(SoundKind.BUTTON_PRESS, 1f)
                }
                onClick()
            }
            .focusable(interactionSource)
            .then(modifier),
        alignment = Alignment.Center,
    ) {
        val colorTheme = colorTheme ?: ColorTheme.dark
        val textStyle = textStyle ?: LocalTextStyle.current.copy(
            //shadow = true,
        )
        CompositionLocalProvider(
            LocalColorTheme provides colorTheme,
            LocalTextStyle provides textStyle,
        ) {
            content()
        }
    }
}
