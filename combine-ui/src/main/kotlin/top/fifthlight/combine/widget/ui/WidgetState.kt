package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.*
import top.fifthlight.combine.input.InteractionSource
import top.fifthlight.combine.modifier.focus.FocusInteraction
import top.fifthlight.combine.modifier.pointer.ClickInteraction
import top.fifthlight.combine.modifier.pointer.DragInteraction
import top.fifthlight.combine.ui.style.NinePatchTextureSet
import top.fifthlight.combine.ui.style.TextureSet

internal enum class WidgetState {
    NORMAL,
    FOCUS,
    HOVER,
    ACTIVE,
}

internal fun TextureSet.getByState(state: WidgetState, disabled: Boolean = false) = if (disabled) {
    this.disabled
} else {
    when (state) {
        WidgetState.NORMAL -> normal
        WidgetState.HOVER -> hover
        WidgetState.ACTIVE -> active
        WidgetState.FOCUS -> focus
    }
}

internal fun NinePatchTextureSet.getByState(state: WidgetState, disabled: Boolean = false) = if (disabled) {
    this.disabled
} else {
    when (state) {
        WidgetState.NORMAL -> normal
        WidgetState.HOVER -> hover
        WidgetState.ACTIVE -> active
        WidgetState.FOCUS -> focus
    }
}

private enum class InteractionType(val priority: Int) {
    Empty(0),
    Focus(1),
    Hover(2),
    Active(3);

    operator fun plus(other: InteractionType): InteractionType = if (other.priority > priority) {
        other
    } else {
        this
    }
}

private val ClickInteraction.type
    get() = when (this) {
        ClickInteraction.Empty -> InteractionType.Empty
        ClickInteraction.Hover -> InteractionType.Hover
        ClickInteraction.Active -> InteractionType.Active
    }

private val DragInteraction.type
    get() = when (this) {
        DragInteraction.Empty -> InteractionType.Empty
        DragInteraction.Hover -> InteractionType.Hover
        DragInteraction.Active -> InteractionType.Active
    }

private val FocusInteraction.type
    get() = when (this) {
        FocusInteraction.Blur -> InteractionType.Empty
        FocusInteraction.Focus -> InteractionType.Focus
    }

@Composable
internal fun widgetState(interactionSource: InteractionSource): State<WidgetState> {
    var state = remember { mutableStateOf(WidgetState.NORMAL) }
    var lastClickInteraction by remember { mutableStateOf<ClickInteraction>(ClickInteraction.Empty) }
    var lastDragInteraction by remember { mutableStateOf<DragInteraction>(DragInteraction.Empty) }
    var lastFocusInteraction by remember { mutableStateOf<FocusInteraction>(FocusInteraction.Blur) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is ClickInteraction) {
                lastClickInteraction = it
            }
            if (it is DragInteraction) {
                lastDragInteraction = it
            }
            if (it is FocusInteraction) {
                lastFocusInteraction = it
            }
            val clickType = lastClickInteraction.type
            val dragType = lastDragInteraction.type
            val focusType = lastFocusInteraction.type
            state.value = when (clickType + dragType + focusType) {
                InteractionType.Empty -> WidgetState.NORMAL
                InteractionType.Focus -> WidgetState.FOCUS
                InteractionType.Hover -> WidgetState.HOVER
                InteractionType.Active -> WidgetState.ACTIVE
            }
        }
    }
    return state
}