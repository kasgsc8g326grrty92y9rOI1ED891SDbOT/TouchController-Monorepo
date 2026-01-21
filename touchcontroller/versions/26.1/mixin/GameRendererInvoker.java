package top.fifthlight.touchcontroller.version_26_1.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
    @Invoker
    float callGetFov(Camera camera, float partialTicks, boolean applyEffects);
}