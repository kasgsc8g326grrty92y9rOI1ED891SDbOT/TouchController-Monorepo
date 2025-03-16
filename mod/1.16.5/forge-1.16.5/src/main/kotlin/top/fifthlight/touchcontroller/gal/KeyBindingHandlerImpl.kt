package top.fifthlight.touchcontroller.gal

import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.text.TranslationTextComponent
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.platform.TextImpl
import top.fifthlight.touchcontroller.common.gal.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.gal.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.KeyBindingState
import top.fifthlight.touchcontroller.helper.ClickableKeyBinding
import top.fifthlight.touchcontroller.mixin.KeyMappingGetterMixin

private fun KeyBinding.click() {
    (this as ClickableKeyBinding).`touchController$click`()
}

private fun KeyBinding.getClickCount() = (this as ClickableKeyBinding).`touchController$getClickCount`()

private class KeyBindingStateImpl(
    private val keyBinding: KeyBinding,
) : KeyBindingState {
    private var passedClientTick = false

    fun renderTick() {
        if (passedClientTick) {
            wasClicked = clicked || locked
            clicked = false
            passedClientTick = false
        }
    }

    fun clientTick() {
        passedClientTick = true
    }

    override val id: String
        get() = keyBinding.name

    override val name: Text
        get() = TextImpl(TranslationTextComponent(keyBinding.name))

    override val categoryId: String
        get() = keyBinding.category

    override val categoryName: Text
        get() = TextImpl(TranslationTextComponent(keyBinding.category))

    override fun click() {
        keyBinding.click()
    }

    override fun haveClickCount(): Boolean = keyBinding.getClickCount() > 0

    private var wasClicked: Boolean = false

    override var clicked: Boolean = false
        set(value) {
            if (!locked && !wasClicked && !field && value) {
                click()
            }
            field = value
        }

    override var locked: Boolean = false
        set(value) {
            if (!clicked && !field && value) {
                click()
            }
            field = value
        }
}

object KeyBindingHandlerImpl : KeyBindingHandler {
    private val client = Minecraft.getInstance()
    private val options = client.options
    private val state = mutableMapOf<KeyBinding, KeyBindingStateImpl>()

    private fun DefaultKeyBindingType.toMinecraft() = when (this) {
        DefaultKeyBindingType.ATTACK -> options.keyAttack
        DefaultKeyBindingType.USE -> options.keyUse
        DefaultKeyBindingType.INVENTORY -> options.keyInventory
        DefaultKeyBindingType.SWAP_HANDS -> options.keySwapOffhand
        DefaultKeyBindingType.SNEAK -> options.keyShift
        DefaultKeyBindingType.SPRINT -> options.keySprint
        DefaultKeyBindingType.JUMP -> options.keyJump
        DefaultKeyBindingType.PLAYER_LIST -> options.keyPlayerList
    }

    fun isDown(key: KeyBinding) = state[key]?.let { it.clicked || it.locked } == true

    private fun getState(key: KeyBinding) = state.getOrPut(key) {
        KeyBindingStateImpl(key)
    }

    override fun renderTick() {
        for (state in state.values) {
            state.renderTick()
        }
    }

    override fun clientTick() {
        for (state in state.values) {
            state.clientTick()
        }
    }

    override fun getState(type: DefaultKeyBindingType): KeyBindingState {
        return getState(type.toMinecraft())
    }

    override fun getState(id: String): KeyBindingState? =
        KeyMappingGetterMixin.`touchcontroller$getAllKeyMappings`()[id]?.let { getState(it) }

    override fun getAllStates(): Map<String, KeyBindingState> =
        KeyMappingGetterMixin.`touchcontroller$getAllKeyMappings`().mapValues { (_, key) -> getState(key) }
}
