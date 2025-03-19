package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.animation.animateFloatAsState
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.coerceIn
import top.fifthlight.combine.modifier.focus.focusable
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.modifier.pointer.toggleable
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.sound.LocalSoundManager
import top.fifthlight.combine.sound.SoundKind
import top.fifthlight.combine.sound.SoundManager
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.widget.base.Canvas
import top.fifthlight.combine.widget.base.layout.Box
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.touchcontroller.assets.Textures
import kotlin.math.roundToInt

data class SwitchDrawableSet(
    val off: DrawableSet,
    val on: DrawableSet,
    val handle: DrawableSet
)

val defaultSwitchDrawable = SwitchDrawableSet(
    off = DrawableSet(
        normal = Textures.WIDGET_SWITCH_SWITCH_OFF,
        focus = Textures.WIDGET_SWITCH_SWITCH_OFF_HOVER,
        hover = Textures.WIDGET_SWITCH_SWITCH_OFF_HOVER,
        active = Textures.WIDGET_SWITCH_SWITCH_OFF_ACTIVE,
        disabled = Textures.WIDGET_SWITCH_SWITCH_OFF_DISABLED,
    ),
    on = DrawableSet(
        normal = Textures.WIDGET_SWITCH_SWITCH_ON,
        focus = Textures.WIDGET_SWITCH_SWITCH_ON_HOVER,
        hover = Textures.WIDGET_SWITCH_SWITCH_ON_HOVER,
        active = Textures.WIDGET_SWITCH_SWITCH_ON_ACTIVE,
        disabled = Textures.WIDGET_SWITCH_SWITCH_ON_DISABLED,
    ),
    handle = DrawableSet(
        normal = Textures.WIDGET_HANDLE_HANDLE,
        focus = Textures.WIDGET_HANDLE_HANDLE_HOVER,
        hover = Textures.WIDGET_HANDLE_HANDLE_HOVER,
        active = Textures.WIDGET_HANDLE_HANDLE_ACTIVE,
        disabled = Textures.WIDGET_HANDLE_HANDLE_DISABLED,
    ),
)

val LocalSwitchDrawable = staticCompositionLocalOf<SwitchDrawableSet> { defaultSwitchDrawable }

@Composable
fun Switch(
    modifier: Modifier = Modifier,
    drawableSet: SwitchDrawableSet = LocalSwitchDrawable.current,
    enabled: Boolean = true,
    value: Boolean,
    clickSound: Boolean = true,
    onValueChanged: ((Boolean) -> Unit)?,
) {
    val soundManager: SoundManager = LocalSoundManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val state by widgetState(interactionSource)
    val onDrawable = drawableSet.on.getByState(state, enabled = enabled)
    val offDrawable = drawableSet.off.getByState(state, enabled = enabled)
    val handleDrawable = drawableSet.handle.getByState(state, enabled = enabled)

    val modifier = if (onValueChanged == null || !enabled) {
        modifier
    } else {
        Modifier
            .toggleable(
                interactionSource,
                value,
            ) {
                if (clickSound) {
                    soundManager.play(SoundKind.BUTTON_PRESS, 1f)
                }
                onValueChanged(it)
            }
            .focusable(interactionSource)
            .then(modifier)
    }

    val handleValue by animateFloatAsState(if (value) 1f else 0f)

    Canvas(
        modifier = modifier,
        measurePolicy = { _, constraints -> layout(onDrawable.size.coerceIn(constraints)) {} },
    ) { node ->
        offDrawable.run { draw(IntRect(offset = IntOffset.ZERO, size = node.size)) }
        onDrawable.run {
            draw(
                rect = IntRect(offset = IntOffset.ZERO, size = node.size),
                tint = Color(handleValue, 1f, 1f, 1f),
            )
        }
        val handleSize = handleDrawable.size
        val handleOffset = IntOffset(
            x = ((node.size.width - handleSize.width) * handleValue).roundToInt(),
            y = (node.size.height - handleSize.height) / 2,
        )
        handleDrawable.run { draw(IntRect(offset = handleOffset, size = handleSize)) }
    }
}