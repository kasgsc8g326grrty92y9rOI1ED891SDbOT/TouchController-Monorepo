package top.fifthlight.touchcontroller.common.layout

import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.control.PlayerListButton
import top.fifthlight.touchcontroller.common.control.PlayerListButtonTexture
import top.fifthlight.touchcontroller.common.gal.DefaultKeyBindingType

fun Context.PlayerListButton(config: PlayerListButton) {
    KeyMappingButton(id = config.id, keyType = DefaultKeyBindingType.PLAYER_LIST) { clicked ->
        withAlign(align = Align.CENTER_CENTER, size = size) {
            when (config.texture) {
                PlayerListButtonTexture.CLASSIC -> Texture(Textures.CONTROL_CLASSIC_PLAYER_LIST_PLAYER_LIST)
                PlayerListButtonTexture.NEW -> Texture(Textures.CONTROL_NEW_PLAYER_LIST_PLAYER_LIST)
            }
        }
    }
}