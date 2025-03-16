package top.fifthlight.touchcontroller.common.layout

import top.fifthlight.combine.paint.Color
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.control.AscendButton
import top.fifthlight.touchcontroller.common.control.AscendButtonTexture
import top.fifthlight.touchcontroller.common.gal.DefaultKeyBindingType

fun Context.AscendButton(config: AscendButton) {
    KeyMappingSwipeButton(id = config.id, keyType = DefaultKeyBindingType.JUMP) { clicked ->
        when (Pair(config.texture, clicked)) {
            Pair(AscendButtonTexture.CLASSIC, false) -> Texture(texture = Textures.CONTROL_CLASSIC_ASCEND_ASCEND)
            Pair(AscendButtonTexture.CLASSIC, true) -> Texture(
                texture = Textures.CONTROL_CLASSIC_ASCEND_ASCEND,
                tint = Color(0xFFAAAAAAu),
            )

            Pair(AscendButtonTexture.SWIMMING, false) -> Texture(texture = Textures.CONTROL_NEW_ASCEND_ASCEND_SWIMMING)
            Pair(
                AscendButtonTexture.SWIMMING,
                true
            ) -> Texture(texture = Textures.CONTROL_NEW_ASCEND_ASCEND_SWIMMING_ACTIVE)

            Pair(AscendButtonTexture.FLYING, false) -> Texture(texture = Textures.CONTROL_NEW_ASCEND_ASCEND)
            Pair(
                AscendButtonTexture.FLYING,
                true
            ) -> Texture(texture = Textures.CONTROL_NEW_ASCEND_ASCEND_ACTIVE)
        }
    }
}