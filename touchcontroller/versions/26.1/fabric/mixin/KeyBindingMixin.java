package top.fifthlight.touchcontroller.version_26_1.fabric.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;
import top.fifthlight.touchcontroller.version_26_1.extensions.ClickableKeyBinding;
import top.fifthlight.touchcontroller.version_26_1.fabric.TouchController;
import top.fifthlight.touchcontroller.version_26_1.fabric.gal.KeyBindingHandlerImpl;

import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class KeyBindingMixin implements ClickableKeyBinding {
    @Shadow
    @Final
    private static Map<InputConstants.Key, KeyMapping> MAP;

    @Shadow
    private int clickCount;

    @Unique
    private static boolean touchController$doCancelKey(InputConstants.Key key) {
        var configHolder = GlobalConfigHolder.INSTANCE;
        var config = configHolder.getConfig().getValue();

        var client = Minecraft.getInstance();
        KeyMapping keyBinding = MAP.get(key);

        if (keyBinding == client.options.keyAttack || keyBinding == client.options.keyUse) {
            return config.getRegular().getDisableMouseClick() || config.getDebug().getEnableTouchEmulation();
        }

        for (var i = 0; i < 9; i++) {
            if (client.options.keyHotbarSlots[i] == keyBinding) {
                return config.getRegular().getDisableHotBarKey();
            }
        }

        return false;
    }

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void onKeyPressed(InputConstants.Key key, CallbackInfo info) {
        if (touchController$doCancelKey(key)) {
            info.cancel();
        }
    }

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void setKeyPressed(InputConstants.Key key, boolean pHeld, CallbackInfo info) {
        if (touchController$doCancelKey(key)) {
            info.cancel();
        }
    }

    @Override
    public void touchController$click() {
        clickCount++;
    }

    @Override
    public int touchController$getClickCount() {
        return clickCount;
    }

    @Inject(
            method = "isDown()Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void overrideIsDown(CallbackInfoReturnable<Boolean> info) {
        if (KeyBindingHandlerImpl.INSTANCE.isDown((KeyMapping) (Object) this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(
            method = "setDown(Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockEmulatedKeyDown(boolean value, CallbackInfo ci) {
        if (TouchController.isInEmulatedSetDown()) {
            ci.cancel();
        }
    }
}