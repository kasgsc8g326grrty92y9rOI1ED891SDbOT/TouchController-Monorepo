package top.fifthlight.touchcontroller.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.combine.platform.TextImpl;
import top.fifthlight.touchcontroller.ui.screen.ConfigScreenKt;

@Mixin(ControlsOptionsScreen.class)
public abstract class ControlsOptionsScreenMixin {
    @Inject(at = @At("TAIL"), method = "addOptions")
    protected void addOptions(CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        var screen = (ControlsOptionsScreen) (Object) this;
        var body = ((GameOptionsScreenAccessor) this).body();
        var textObj = ConfigScreenKt.getConfigScreenButtonText();
        if (textObj instanceof TextImpl) {
            textObj = ((TextImpl) textObj).getInner();
        }
        Text text = (Text) textObj;
        body.addWidgetEntry(
                ButtonWidget.builder(
                        text,
                        btn -> client.setScreen((Screen) ConfigScreenKt.getConfigScreen(screen))
                ).build(), null
        );
    }
}