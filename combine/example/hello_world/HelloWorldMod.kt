package top.fifthlight.combine.example.helloworld

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.Screen
import org.lwjgl.glfw.GLFW
import top.fifthlight.combine.data.TextFactoryFactory
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.screen.ScreenFactoryFactory
import top.fifthlight.combine.theme.invoke
import top.fifthlight.combine.theme.vanilla.VanillaTheme
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.ui.Button
import top.fifthlight.combine.widget.ui.Text

class HelloWorldMod: ClientModInitializer, ModMenuApi {
    private val keyMapping = KeyMapping("combine_hello_world", GLFW.GLFW_KEY_H, "combine_example")

    private fun createScreen(parent: Screen? = null) = ScreenFactoryFactory.of().getScreen(
        parent = parent,
        title = TextFactoryFactory.of().literal("Hello world")) {
        VanillaTheme {
            Box(
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4),
                ) {
                    var i by remember { mutableStateOf(0) }
                    Text("Counter: $i")
                    Button(onClick = {
                        i++
                    }) {
                        Text("+")
                    }
                }
            }
        }
    } as Screen

    override fun getModConfigScreenFactory() = ConfigScreenFactory(::createScreen)

    override fun onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(keyMapping)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!keyMapping.isDown) {
                return@register
            }
            if (client.screen != null) {
                return@register
            }
            client.setScreen(createScreen(null))
        }
    }
}