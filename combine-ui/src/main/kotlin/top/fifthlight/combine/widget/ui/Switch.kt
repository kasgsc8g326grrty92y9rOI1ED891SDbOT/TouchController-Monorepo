package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.focus.focusable
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.ui.style.TextureSet
import top.fifthlight.combine.widget.base.layout.Box
import top.fifthlight.touchcontroller.assets.Textures

data class SwitchTextureSet(
    val off: TextureSet,
    val on: TextureSet,
    val handle: TextureSet
)

val defaultSwitchTexture = SwitchTextureSet(
    off = TextureSet(
        normal = Textures.WIDGET_SWITCH_SWITCH_OFF,
        focus = Textures.WIDGET_SWITCH_SWITCH_OFF_HOVER,
        hover = Textures.WIDGET_SWITCH_SWITCH_OFF_HOVER,
        active = Textures.WIDGET_SWITCH_SWITCH_OFF_ACTIVE,
        disabled = Textures.WIDGET_SWITCH_SWITCH_OFF_DISABLED,
    ),
    on = TextureSet(
        normal = Textures.WIDGET_SWITCH_SWITCH_ON,
        focus = Textures.WIDGET_SWITCH_SWITCH_ON_HOVER,
        hover = Textures.WIDGET_SWITCH_SWITCH_ON_HOVER,
        active = Textures.WIDGET_SWITCH_SWITCH_ON_ACTIVE,
        disabled = Textures.WIDGET_SWITCH_SWITCH_ON_DISABLED,
    ),
    handle = TextureSet(
        normal = Textures.WIDGET_HANDLE_HANDLE,
        focus = Textures.WIDGET_HANDLE_HANDLE_HOVER,
        hover = Textures.WIDGET_HANDLE_HANDLE_HOVER,
        active = Textures.WIDGET_HANDLE_HANDLE_ACTIVE,
        disabled = Textures.WIDGET_HANDLE_HANDLE_DISABLED,
    ),
)

val LocalSwitchTexture = staticCompositionLocalOf<SwitchTextureSet> { defaultSwitchTexture }

@Composable
fun Switch(
    modifier: Modifier = Modifier,
    textureSet: SwitchTextureSet = LocalSwitchTexture.current,
    enabled: Boolean = true,
    value: Boolean,
    onValueChanged: ((Boolean) -> Unit)?,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val state by widgetState(interactionSource)
    val texture = if (value) {
        textureSet.on.getByState(state, enabled = enabled)
    } else {
        textureSet.off.getByState(state, enabled = enabled)
    }
    val handleTexture = textureSet.handle.getByState(state, enabled = enabled)

    val modifier = if (onValueChanged == null || !enabled) {
        modifier
    } else {
        Modifier
            .clickable(interactionSource) {
                onValueChanged(!value)
            }
            .focusable(interactionSource)
            .then(modifier)
    }

    Box(
        modifier = modifier,
        alignment = if (value) {
            Alignment.CenterRight
        } else {
            Alignment.CenterLeft
        }
    ) {
        Icon(texture = texture)
        Icon(texture = handleTexture)
    }
}