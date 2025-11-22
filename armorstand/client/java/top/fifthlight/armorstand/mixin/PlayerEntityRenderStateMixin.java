package top.fifthlight.armorstand.mixin;

import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import top.fifthlight.armorstand.extension.internal.PlayerRenderStateExtInternal;
import top.fifthlight.blazerod.api.animation.AnimationItemPendingValues;

import java.util.UUID;

@Mixin(PlayerRenderState.class)
public abstract class PlayerEntityRenderStateMixin implements PlayerRenderStateExtInternal {
    @Unique
    private UUID uuid;

    @Unique
    private AnimationItemPendingValues animationPendingValues;

    @Override
    public void armorstand$setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID armorstand$getUuid() {
        return uuid;
    }

    @Override
    public void armorstand$setAnimationPendingValues(AnimationItemPendingValues pendingValues) {
        this.animationPendingValues = pendingValues;
    }

    @Override
    @Nullable
    public AnimationItemPendingValues armorstand$getAnimationPendingValues() {
        return animationPendingValues;
    }
}
