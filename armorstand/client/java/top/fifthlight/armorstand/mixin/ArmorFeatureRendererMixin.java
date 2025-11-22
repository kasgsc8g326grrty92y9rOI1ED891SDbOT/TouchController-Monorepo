package top.fifthlight.armorstand.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.armorstand.config.ConfigHolder;

@Mixin(HumanoidArmorLayer.class)
public class ArmorFeatureRendererMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    public void disableRendering(PoseStack poseStack, MultiBufferSource bufferSource, int light, HumanoidRenderState state, float limbAngle, float limbDistance, CallbackInfo ci) {
        if (ConfigHolder.INSTANCE.getConfig().getValue().getHidePlayerArmor()) {
            ci.cancel();
        }
    }
}
