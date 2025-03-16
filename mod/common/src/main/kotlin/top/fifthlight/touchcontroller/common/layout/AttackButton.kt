package top.fifthlight.touchcontroller.common.layout

import top.fifthlight.combine.paint.Color
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.control.AttackButton
import top.fifthlight.touchcontroller.common.control.AttackButtonTexture
import top.fifthlight.touchcontroller.common.gal.DefaultKeyBindingType

fun Context.AttackButton(config: AttackButton) {
    KeyMappingButton(id = config.id, keyType = DefaultKeyBindingType.ATTACK) { clicked ->
        withAlign(align = Align.CENTER_CENTER, size = size) {
            when (config.texture) {
                AttackButtonTexture.CLASSIC -> {
                    if (clicked) {
                        Texture(texture = Textures.CONTROL_CLASSIC_ATTACK_ATTACK, tint = Color(0xFFAAAAAAu))
                    } else {
                        Texture(texture = Textures.CONTROL_CLASSIC_ATTACK_ATTACK)
                    }
                }

                AttackButtonTexture.NEW -> {
                    if (clicked) {
                        Texture(texture = Textures.CONTROL_NEW_ATTACK_ATTACK_ACTIVE)
                    } else {
                        Texture(texture = Textures.CONTROL_NEW_ATTACK_ATTACK)
                    }
                }
            }
        }
    }
}