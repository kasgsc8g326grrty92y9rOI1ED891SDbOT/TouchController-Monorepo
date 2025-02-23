package top.fifthlight.touchcontroller.layout

import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.control.InventoryButton
import top.fifthlight.touchcontroller.gal.KeyBindingType
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun Context.InventoryButton(config: InventoryButton) {
    val (_, _, release) = Button(id = config.id) { clicked ->
        if (config.classic) {
            if (clicked) {
                Texture(texture = Textures.CONTROL_CLASSIC_INVENTORY_INVENTORY_ACTIVE)
            } else {
                Texture(texture = Textures.CONTROL_CLASSIC_INVENTORY_INVENTORY)
            }
        } else {
            if (clicked) {
                Texture(texture = Textures.CONTROL_NEW_INVENTORY_INVENTORY_ACTIVE)
            } else {
                Texture(texture = Textures.CONTROL_NEW_INVENTORY_INVENTORY)
            }
        }
    }
    if (release) {
        keyBindingHandler.getState(KeyBindingType.INVENTORY).clicked = true
    }
}