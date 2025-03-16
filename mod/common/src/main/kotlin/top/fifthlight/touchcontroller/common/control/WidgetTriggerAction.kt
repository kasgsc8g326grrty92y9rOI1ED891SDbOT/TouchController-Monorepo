package top.fifthlight.touchcontroller.common.control

import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.combine.data.Identifier
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.gal.GameAction
import top.fifthlight.touchcontroller.common.gal.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.PlayerHandle
import top.fifthlight.touchcontroller.common.ui.screen.openChatScreen

@Serializable
sealed class WidgetTriggerAction {
    abstract fun trigger(player: PlayerHandle)
    abstract val actionType: Type

    enum class Type(val nameId: Identifier) {
        NONE(Texts.WIDGET_TRIGGER_NONE),
        KEY(Texts.WIDGET_TRIGGER_KEY),
        GAME(Texts.WIDGET_TRIGGER_GAME_ACTION),
        PLAYER(Texts.WIDGET_TRIGGER_PLAYER_ACTION),
    }

    @Serializable
    @SerialName("key")
    sealed class Key : WidgetTriggerAction() {
        override val actionType
            get() = Type.KEY

        private companion object : KoinComponent {
            val keyBindingHandler: KeyBindingHandler by inject()
        }

        abstract val keyBinding: String?
        protected val keyBindingState by lazy {
            keyBinding?.let { keyBindingHandler.getState(it) }
        }

        @Serializable
        @SerialName("click")
        data class Click(
            override val keyBinding: String? = null,
            val keepInClientTick: Boolean = true,
        ) : Key() {
            override fun trigger(player: PlayerHandle) {
                keyBindingState?.let { keyBindingState ->
                    if (keepInClientTick) {
                        keyBindingState.clicked = true
                    } else {
                        keyBindingState.click()
                    }
                }
            }
        }

        @Serializable
        @SerialName("lock")
        data class Lock(
            override val keyBinding: String? = null,
            val lockType: LockActionType = LockActionType.INVERT,
        ) : Key() {
            @Serializable
            enum class LockActionType(
                val nameId: Identifier,
            ) {
                @SerialName("start")
                START(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE_START),

                @SerialName("stop")
                STOP(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE_STOP),

                @SerialName("invert")
                INVERT(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE_INVERT),
            }

            override fun trigger(player: PlayerHandle) {
                keyBindingState?.let { keyBindingState ->
                    when (lockType) {
                        LockActionType.START -> keyBindingState.locked = true
                        LockActionType.STOP -> keyBindingState.locked = false
                        LockActionType.INVERT -> keyBindingState.locked = !keyBindingState.locked
                    }
                }
            }
        }
    }

    @Serializable
    @SerialName("game")
    sealed class Game : WidgetTriggerAction(), KoinComponent {
        override val actionType
            get() = Type.GAME

        private val gameAction: GameAction by inject()
        final override fun trigger(player: PlayerHandle) = trigger(gameAction)
        abstract fun trigger(gameAction: GameAction)

        abstract val nameId: Identifier

        @Serializable
        @SerialName("vanilla_chat")
        data object VanillaChatScreen : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_VANILLA_CHAT_SCREEN

            override fun trigger(gameAction: GameAction) {
                gameAction.openGameMenu()
            }
        }

        @Serializable
        @SerialName("chat")
        data object ChatScreen : Game(), KoinComponent {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_CHAT_SCREEN

            override fun trigger(gameAction: GameAction) {
                openChatScreen()
            }
        }

        @Serializable
        @SerialName("game_menu")
        data object GameMenu : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_GAME_MENU

            override fun trigger(gameAction: GameAction) {
                gameAction.openGameMenu()
            }
        }

        @Serializable
        @SerialName("next_perspective")
        data object NextPerspective : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_NEXT_PERSPECTIVE

            override fun trigger(gameAction: GameAction) {
                gameAction.nextPerspective()
            }
        }

        @Serializable
        @SerialName("take_screenshot")
        data object TakeScreenshot : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_TAKE_SCREENSHOT

            override fun trigger(gameAction: GameAction) {
                gameAction.takeScreenshot()
            }
        }

        @Serializable
        @SerialName("take_panorama")
        data object TakePanorama : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_TAKE_PANORAMA

            override fun trigger(gameAction: GameAction) {
                gameAction.takePanorama()
            }
        }

        companion object {
            val all by lazy {
                persistentListOf(
                    VanillaChatScreen,
                    ChatScreen,
                    GameMenu,
                    NextPerspective,
                    TakeScreenshot,
                    TakePanorama,
                )
            }
        }
    }

    @Serializable
    @SerialName("player")
    sealed class Player : WidgetTriggerAction() {
        override val actionType
            get() = Type.PLAYER

        abstract val nameId: Identifier

        @Serializable
        @SerialName("cancel_flying")
        data object CancelFlying : Player() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_PLAYER_ACTION_CANCEL_FLYING

            override fun trigger(player: PlayerHandle) {
                player.isFlying = false
            }
        }

        @Serializable
        @SerialName("start_sprint")
        data object StartSprint : Player() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_PLAYER_ACTION_START_SPRINT

            override fun trigger(player: PlayerHandle) {
                player.isSprinting = true
            }
        }

        @Serializable
        @SerialName("stop_sprint")
        data object StopSprint : Player() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_PLAYER_ACTION_STOP_SPRINT

            override fun trigger(player: PlayerHandle) {
                player.isSprinting = false
            }
        }

        companion object {
            val all by lazy {
                persistentListOf(
                    CancelFlying,
                    StartSprint,
                    StopSprint,
                )
            }
        }
    }
}