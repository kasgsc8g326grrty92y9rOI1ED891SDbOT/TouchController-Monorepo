package top.fifthlight.touchcontroller.ui.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.control.*

data class WidgetsTabState(
    val listState: ListState = ListState.Builtin,
    val dialogState: DialogState = DialogState.Empty,
    val newWidgetParams: NewWidgetParams = NewWidgetParams(),
) {
    data class NewWidgetParams(
        val opacity: Float = .6f,
        val textureSet: TextureSet.TextureSetKey = TextureSet.TextureSetKey.CLASSIC,
    )

    sealed class DialogState {
        data object Empty : DialogState()

        data class ChangeNewWidgetParams(
            val opacity: Float = .6f,
            val textureSet: TextureSet.TextureSetKey = TextureSet.TextureSetKey.CLASSIC,
        ) : DialogState() {
            constructor(params: NewWidgetParams) : this(opacity = params.opacity, textureSet = params.textureSet)

            fun toParams() = NewWidgetParams(
                opacity = opacity,
                textureSet = textureSet,
            )
        }
    }

    sealed class ListState {
        open val heroes: PersistentList<ControllerWidget>? = null
        open val widgets: PersistentList<ControllerWidget>? = null

        data object Builtin : ListState() {
            override val heroes = persistentListOf<ControllerWidget>(
                DPad(),
                Joystick()
            )

            override val widgets = persistentListOf<ControllerWidget>(
                AscendButton(),
                DescendButton(),
                BoatButton(),
                ChatButton(),
                DescendButton(),
                ForwardButton(),
                HideHudButton(),
                InventoryButton(),
                JumpButton(),
                PanoramaButton(),
                PauseButton(),
                PerspectiveSwitchButton(),
                PlayerListButton(),
                ScreenshotButton(),
                SneakButton(),
                SprintButton(),
                UseButton(),
            )
        }

        sealed class Custom : ListState() {
            data object Loading : Custom()

            data class Loaded(
                override val widgets: PersistentList<ControllerWidget>? = null,
            ) : Custom()
        }
    }
}