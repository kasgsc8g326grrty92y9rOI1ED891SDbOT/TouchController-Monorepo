package top.fifthlight.touchcontroller.mixin;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyMappingGetterMixin {
    @Accessor("ALL")
    static Map<String, KeyBinding> touchcontroller$getAllKeyMappings() {
        throw new AssertionError();
    }
}