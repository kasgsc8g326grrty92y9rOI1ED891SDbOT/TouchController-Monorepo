package top.fifthlight.armorstand.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.fifthlight.armorstand.event.ScreenEvents;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @WrapWithCondition(
            method = "setScreen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;releaseMouse()V")
    )
    private boolean wrapUnlockCursor(MouseHandler mouse, Screen screen) {
        if (screen != null) {
            return ScreenEvents.UNLOCK_CURSOR.getInvoker().onMouseUnlocked(screen);
        }
        return true;
    }
}
