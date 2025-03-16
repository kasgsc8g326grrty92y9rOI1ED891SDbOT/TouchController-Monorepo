package top.fifthlight.touchcontroller.common.gal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactory

@Serializable
enum class DefaultKeyBindingType {
    @SerialName("attack")
    ATTACK,

    @SerialName("use")
    USE,

    @SerialName("inventory")
    INVENTORY,

    @SerialName("swap_hands")
    SWAP_HANDS,

    @SerialName("sneak")
    SNEAK,

    @SerialName("sprint")
    SPRINT,

    @SerialName("jump")
    JUMP,

    @SerialName("player_list")
    PLAYER_LIST,
}

interface KeyBindingState {
    val id: String
    val name: Text
    val categoryId: String
    val categoryName: Text

    // Click for once. You probably don't want to use this as it only increases press count, without actually pressing
    // the button. If it causes problems, use clicked = true instead.
    fun click()

    fun haveClickCount(): Boolean

    // Click for one tick (client tick). It will be reset every tick.
    var clicked: Boolean

    // Lock between ticks. You can read value from this field to query lock state.
    var locked: Boolean

    companion object Empty : KeyBindingState, KoinComponent {
        private val textFactory: TextFactory by inject()
        override val id: String = "empty"
        override val name: Text
            get() = textFactory.empty()
        override val categoryId: String = "empty"
        override val categoryName: Text
            get() = textFactory.empty()

        override fun click() {}
        override fun haveClickCount() = false
        override var clicked: Boolean
            get() = false
            set(_) {}
        override var locked: Boolean
            get() = false
            set(_) {}
    }
}

interface KeyBindingHandler {
    fun renderTick()
    fun clientTick()
    fun getState(type: DefaultKeyBindingType): KeyBindingState
    fun getState(id: String): KeyBindingState?
    fun getAllStates(): Map<String, KeyBindingState>

    companion object Empty : KeyBindingHandler {
        override fun renderTick() {}
        override fun clientTick() {}
        override fun getState(type: DefaultKeyBindingType) = KeyBindingState.Empty
        override fun getState(id: String): KeyBindingState? = null
        override fun getAllStates(): Map<String, KeyBindingState> = mapOf()
    }
}