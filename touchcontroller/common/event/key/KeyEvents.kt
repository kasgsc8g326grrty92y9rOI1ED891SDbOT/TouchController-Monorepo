package top.fifthlight.touchcontroller.common.event.key

import top.fifthlight.touchcontroller.common.gal.key.KeyBindingState

object KeyEvents {
    private val handlers = mutableListOf<(KeyBindingState) -> Unit>()

    fun addHandler(handler: (KeyBindingState) -> Unit) {
        handlers.add(handler)
    }

    fun onKeyDown(state: KeyBindingState) {
        handlers.forEach {
            it.invoke(state)
        }
    }
}