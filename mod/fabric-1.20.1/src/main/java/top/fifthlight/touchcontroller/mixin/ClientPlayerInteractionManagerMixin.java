package top.fifthlight.touchcontroller.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.koin.java.KoinJavaComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.combine.platform.ItemFactoryImplKt;
import top.fifthlight.touchcontroller.config.GlobalConfigHolder;
import top.fifthlight.touchcontroller.event.BlockBreakEvents;
import top.fifthlight.touchcontroller.helper.CrosshairTargetHelper;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Unique
    private boolean resetPlayerLookTarget;
    @Unique
    private float prevYaw;
    @Unique
    private float prevPitch;
    @Unique
    private PlayerEntity interactItemPlayer;

    @Inject(
            method = "breakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"
            )
    )
    public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockBreakEvents.INSTANCE.afterBlockBreak();
    }
    @Unique
    private Hand interactItemHand;

    @Shadow
    protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

    @Unique
    private boolean crosshairAimingContainItem(Item item) {
        GlobalConfigHolder globalConfigHolder = KoinJavaComponent.get(GlobalConfigHolder.class);
        var globalConfig = globalConfigHolder.getConfig().getValue();
        return ItemFactoryImplKt.contains(globalConfig.getItem().getCrosshairAimingItems(), item);
    }

    @Unique
    public PlayerMoveC2SPacket interactBefore(PlayerEntity player, Hand hand, boolean sendFullPacket) {
        var itemStack = player.getStackInHand(hand);
        if (!crosshairAimingContainItem(itemStack.getItem())) {
            return null;
        }

        prevYaw = player.getYaw();
        prevPitch = player.getPitch();

        var rotation = CrosshairTargetHelper.calculatePlayerRotation(CrosshairTargetHelper.INSTANCE.getLastCrosshairDirection());
        float yaw = rotation.getFirst();
        float pitch = rotation.getSecond();
        player.setYaw(yaw);
        player.setPitch(pitch);
        resetPlayerLookTarget = true;
        if (sendFullPacket) {
            return new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), yaw, pitch, player.isOnGround());
        } else {
            return new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, player.isOnGround());
        }
    }

    @Unique
    public void interactAfter(PlayerEntity player) {
        if (!resetPlayerLookTarget) {
            return;
        }
        resetPlayerLookTarget = false;
        player.setYaw(prevYaw);
        player.setPitch(prevPitch);
        this.sendSequencedPacket(this.client.world, sequence -> new PlayerMoveC2SPacket.LookAndOnGround(player.getYaw(), player.getPitch(), player.isOnGround()));
    }

    @Inject(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V",
                    ordinal = 0
            )
    )
    public void interactBlockBefore(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        interactBefore(player, hand, false);
    }

    @Inject(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    public void interactBlockAfter(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        interactAfter(player);
    }

    @Inject(method = "interactItem", at = @At("HEAD"))
    public void interactItemBefore(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        interactItemPlayer = player;
        interactItemHand = hand;
    }

    @Redirect(
            method = "interactItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
            )
    )
    public void interactItemSendPacket(ClientPlayNetworkHandler instance, Packet<?> packet) {
        var newPacket = interactBefore(interactItemPlayer, interactItemHand, true);
        if (newPacket == null) {
            instance.sendPacket(packet);
        } else {
            instance.sendPacket(newPacket);
        }
        interactItemPlayer = null;
        interactItemHand = null;
    }

    @Inject(
            method = "interactItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    public void interactItemAfter(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        interactAfter(player);
    }
}
