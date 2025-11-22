package top.fifthlight.armorstand.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import top.fifthlight.armorstand.PlayerRenderer;
import top.fifthlight.armorstand.extension.internal.PlayerRenderStateExtInternal;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerModelMixin {
    @Shadow
    public static int getOverlayCoords(LivingEntityRenderState state, float whiteOverlayProgress) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract float getWhiteOverlayProgress(LivingEntityRenderState state);

    @WrapOperation(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/RenderType;"))
    @Nullable
    public <T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
    RenderType render(LivingEntityRenderer<T, S, M> instance,
                      S state,
                      boolean showBody,
                      boolean translucent,
                      boolean showOutline,
                      Operation<RenderType> original,
                      S livingEntityRenderState,
                      PoseStack matrixStack,
                      MultiBufferSource vertexConsumerProvider,
                      int light
    ) {
        if (!(state instanceof PlayerRenderState)) {
            return original.call(instance, state, showBody, translucent, showOutline);
        }
        var uuid = ((PlayerRenderStateExtInternal) state).armorstand$getUuid();
        if (uuid == null) {
            return original.call(instance, state, showBody, translucent, showOutline);
        }
        var overlay = getOverlayCoords(state, getWhiteOverlayProgress(livingEntityRenderState));
        if (!PlayerRenderer.appendPlayer(uuid, (PlayerRenderState) state, matrixStack, vertexConsumerProvider, light, overlay)) {
            return original.call(instance, state, showBody, translucent, showOutline);
        }
        return null;
    }
}
