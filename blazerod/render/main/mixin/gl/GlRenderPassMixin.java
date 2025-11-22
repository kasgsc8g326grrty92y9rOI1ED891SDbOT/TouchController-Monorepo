package top.fifthlight.blazerod.mixin.gl;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.opengl.GlRenderPass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.blazerod.extension.internal.RenderPassExtInternal;

import java.util.HashMap;
import java.util.Map;

@Mixin(GlRenderPass.class)
public abstract class GlRenderPassMixin implements RenderPassExtInternal {
    @Shadow
    private boolean closed;

    @Shadow
    @Final
    private GlCommandEncoder encoder;

    @Unique
    private HashMap<String, GpuBufferSlice> storageBuffers;

    @Unique
    private VertexFormat vertexFormat;

    @Unique
    private VertexFormat.Mode vertexFormatMode;

    @Override
    public void blazerod$setVertexFormat(VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;
    }

    @Override
    @Nullable
    public VertexFormat blazerod$getVertexFormat() {
        return vertexFormat;
    }

    @Override
    public void blazerod$setVertexFormatMode(VertexFormat.Mode vertexFormatMode) {
        this.vertexFormatMode = vertexFormatMode;
    }

    @Override
    @Nullable
    public VertexFormat.Mode blazerod$getVertexFormatMode() {
        return vertexFormatMode;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void afterInit(GlCommandEncoder resourceManager, boolean hasDepth, CallbackInfo ci) {
        storageBuffers = new HashMap<>();
    }

    @Override
    public void blazerod$setStorageBuffer(@NotNull String name, GpuBufferSlice buffer) {
        storageBuffers.put(name, buffer);
    }

    @NotNull
    @Override
    public Map<String, GpuBufferSlice> blazerod$getStorageBuffers() {
        return storageBuffers;
    }

    @Override
    public void blazerod$draw(int baseVertex, int firstIndex, int count, int instanceCount) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        } else {
            this.encoder.executeDraw((GlRenderPass) (Object) this, baseVertex, firstIndex, count, null, instanceCount);
        }
    }
}
