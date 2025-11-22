package top.fifthlight.armorstand

import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

interface ArmorStandClient : ArmorStand {
    companion object {
        lateinit var instance: ArmorStandClient

        val configKeyBinding by lazy {
            KeyMapping(
                "armorstand.keybinding.config",
                GLFW.GLFW_KEY_I,
                "armorstand.name"
            )
        }
        val animationKeyBinding by lazy {
            KeyMapping(
                "armorstand.keybinding.animation",
                GLFW.GLFW_KEY_K,
                "armorstand.name"
            )
        }
        val modelSwitchKeyBinding by lazy {
            KeyMapping(
                "armorstand.keybinding.model_switch",
                GLFW.GLFW_KEY_U,
                "armorstand.name"
            )
        }
    }

    val debug: Boolean
    val debugBone: Boolean
}
