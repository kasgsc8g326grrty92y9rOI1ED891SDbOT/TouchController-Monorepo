package top.fifthlight.armorstand.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.fifthlight.armorstand.config.ConfigHolder;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @WrapOperation(
            method = "render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRenderShadow:Z")
    )
    public boolean renderShadows(EntityRenderDispatcher instance, Operation<Boolean> original, EntityRenderState state) {
        if (!(state instanceof PlayerRenderState)) {
            return original.call(instance);
        }
        if (ConfigHolder.INSTANCE.getConfig().getValue().getHidePlayerShadow()) {
            return false;
        } else {
            return original.call(instance);
        }
    }
}
