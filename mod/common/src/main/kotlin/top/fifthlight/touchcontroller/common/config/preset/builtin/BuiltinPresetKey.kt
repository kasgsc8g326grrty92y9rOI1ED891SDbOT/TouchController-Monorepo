package top.fifthlight.touchcontroller.common.config.preset.builtin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.common.config.controllerLayoutOf
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.PresetControlInfo
import top.fifthlight.touchcontroller.common.control.CustomWidget
import top.fifthlight.touchcontroller.common.control.InventoryButton

@Serializable
data class BuiltinPresetKey(
    @SerialName("texture_set")
    val textureSet: TextureSet.TextureSetKey = TextureSet.TextureSetKey.CLASSIC,
    @SerialName("control_style")
    val controlStyle: ControlStyle = ControlStyle.TouchGesture,
    @SerialName("move_method")
    val moveMethod: MoveMethod = MoveMethod.Dpad(),
    @SerialName("sprint_button_location")
    val sprintButtonLocation: SprintButtonLocation = SprintButtonLocation.NONE,
    @SerialName("opacity")
    val opacity: Float = .6f,
    @SerialName("scale")
    val scale: Float = 1f,
) {
    @Serializable
    sealed class ControlStyle {
        @Serializable
        @SerialName("touch_gesture")
        data object TouchGesture : ControlStyle()

        @Serializable
        @SerialName("split_controls")
        data class SplitControls(
            val buttonInteraction: Boolean = true,
        ) : ControlStyle()
    }

    @Serializable
    enum class SprintButtonLocation(
        val nameId: Identifier,
    ) {
        @SerialName("none")
        NONE(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_NONE),

        @SerialName("right_top")
        RIGHT_TOP(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_RIGHT_TOP),

        @SerialName("right")
        RIGHT(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_RIGHT),
    }

    @Serializable
    sealed class MoveMethod {
        @Serializable
        @SerialName("dpad")
        data class Dpad(
            val swapJumpAndSneak: Boolean = false,
        ) : MoveMethod()

        @Serializable
        @SerialName("joystick")
        data class Joystick(
            val triggerSprint: Boolean = false,
        ) : MoveMethod()
    }

    val preset by lazy {
        val sprintButton = when (sprintButtonLocation) {
            SprintButtonLocation.NONE -> null
            SprintButtonLocation.RIGHT_TOP -> BuiltinLayers.sprintRightTopButton
            SprintButtonLocation.RIGHT -> BuiltinLayers.sprintRightButton
        }
        LayoutPreset(
            name = "TODO",
            controlInfo = PresetControlInfo(
                splitControls = controlStyle is ControlStyle.SplitControls,
                disableTouchGesture = controlStyle is ControlStyle.SplitControls && controlStyle.buttonInteraction,
            ),
            layout = controllerLayoutOf(
                BuiltinLayers.controlLayer,
                BuiltinLayers.interactionLayer.takeIf { controlStyle is ControlStyle.SplitControls && controlStyle.buttonInteraction },
                BuiltinLayers.normalLayer.getByKey(this) + sprintButton,
                BuiltinLayers.swimmingLayer.getByKey(this),
                BuiltinLayers.flyingLayer.getByKey(this),
                BuiltinLayers.onBoatLayer.getByKey(this),
                BuiltinLayers.onMinecartLayer.getByKey(this),
                BuiltinLayers.ridingOnEntityLayer.getByKey(this),
            )
        ).mapWidgets { widget ->
            when (widget) {
                // TODO
                is CustomWidget -> widget
                is InventoryButton -> widget
                else -> widget.cloneBase(opacity = opacity)
            }
        }
    }

    companion object {
        val DEFAULT = BuiltinPresetKey()
    }
}