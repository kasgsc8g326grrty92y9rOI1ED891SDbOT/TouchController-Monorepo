package top.fifthlight.blazerod.mixin.gl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlDebugLabel;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.*;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.blazerod.extension.ShaderTypeExt;
import top.fifthlight.blazerod.extension.internal.GpuBufferExtInternal;
import top.fifthlight.blazerod.extension.internal.RenderPipelineExtInternal;
import top.fifthlight.blazerod.extension.internal.gl.GpuDeviceExtInternal;
import top.fifthlight.blazerod.extension.internal.gl.ShaderProgramExtInternal;
import top.fifthlight.blazerod.render.gl.ShaderProgramExt;
import top.fifthlight.blazerod.systems.ComputePipeline;
import top.fifthlight.blazerod.systems.gl.CompiledComputePipeline;
import top.fifthlight.blazerod.util.glsl.GlslExtensionProcessor;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mixin(GlDevice.class)
public abstract class GlDeviceMixin implements GpuDeviceExtInternal {
    @Unique
    private static final boolean allowGlTextureBufferRange = true;
    @Unique
    private static final boolean allowGlShaderStorageBufferObject = true;
    @Unique
    private static final boolean allowSsboInVertexShader = true;
    @Unique
    private static final boolean allowSsboInFragmentShader = true;
    @Unique
    private static final boolean allowGlComputeShader = true;
    @Unique
    private static final boolean allowGlShaderImageLoadStore = true;
    @Unique
    private static final boolean allowGlShaderPacking = true;

    @Shadow
    @Final
    private Set<String> enabledExtensions;
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private BiFunction<ResourceLocation, ShaderType, String> defaultShaderSource;
    @Shadow
    @Final
    private GlDebugLabel debugLabels;

    @Unique
    private final Map<ComputePipeline, CompiledComputePipeline> computePipelineCompileCache = new IdentityHashMap<>();
    @Unique
    private int glMajorVersion;
    @Unique
    private int glMinorVersion;

    @Unique
    private boolean supportTextureBufferSlice;
    @Unique
    private boolean supportSsbo;
    @Unique
    private boolean supportComputeShader;
    @Unique
    private boolean supportShaderImageLoadStore;
    @Unique
    private boolean supportShaderPacking;
    @Unique
    private int maxSsboBindings;
    @Unique
    private int maxSsboInVertexShader;
    @Unique
    private int maxSsboInFragmentShader;
    @Unique
    private int ssboOffsetAlignment;
    @Unique
    private int textureBufferOffsetAlignment;

    @Shadow
    public abstract GpuBuffer createBuffer(@Nullable Supplier<String> labelSupplier, int usage, int size);

    @Shadow
    public abstract GpuBuffer createBuffer(@Nullable Supplier<String> labelSupplier, int usage, ByteBuffer data);

    @Shadow
    protected abstract GlShaderModule getOrCompileShader(ResourceLocation id, ShaderType type, ShaderDefines defines, BiFunction<ResourceLocation, ShaderType, String> sourceRetriever);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(long contextId, int debugVerbosity, boolean sync, BiFunction<ResourceLocation, ShaderType, String> shaderSourceGetter, boolean debugLabels, CallbackInfo ci, @Local(ordinal = 0) GLCapabilities glCapabilities) {
        if (allowGlTextureBufferRange && glCapabilities.GL_ARB_texture_buffer_range) {
            enabledExtensions.add("GL_ARB_texture_buffer_range");
            supportTextureBufferSlice = true;
        } else {
            supportTextureBufferSlice = false;
        }

        if (allowGlShaderStorageBufferObject
                && glCapabilities.GL_ARB_shader_storage_buffer_object
                && glCapabilities.GL_ARB_program_interface_query
                // OpenGL spec says GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS must be at least 8, but we check it
                // just in case some drivers don't follow the spec
                && GL11.glGetInteger(GL43C.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS) > 8) {
            enabledExtensions.add("GL_ARB_shader_storage_buffer_object");
            enabledExtensions.add("GL_ARB_program_interface_query");
            supportSsbo = true;
        } else {
            supportSsbo = false;
        }

        if (allowGlComputeShader && glCapabilities.GL_ARB_compute_shader) {
            enabledExtensions.add("GL_ARB_compute_shader");
            supportComputeShader = true;
        } else {
            supportComputeShader = false;
        }

        if (allowGlShaderImageLoadStore && glCapabilities.GL_ARB_shader_image_load_store) {
            enabledExtensions.add("GL_ARB_shader_image_load_store");
            supportShaderImageLoadStore = true;
        } else {
            supportShaderImageLoadStore = false;
        }

        if (allowGlShaderPacking && glCapabilities.GL_ARB_shading_language_packing) {
            enabledExtensions.add("GL_ARB_shading_language_packing");
            supportShaderPacking = true;
        } else {
            supportShaderPacking = false;
        }

        if (supportSsbo && allowSsboInVertexShader) {
            maxSsboInVertexShader = GL11.glGetInteger(GL43C.GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS);
        } else {
            maxSsboInVertexShader = 0;
        }
        if (supportSsbo && allowSsboInFragmentShader) {
            maxSsboInFragmentShader = GL11.glGetInteger(GL43C.GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS);
        } else {
            maxSsboInFragmentShader = 0;
        }
        if (supportSsbo) {
            maxSsboBindings = GL11.glGetInteger(GL43C.GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS);
        } else {
            maxSsboBindings = 0;
        }

        if (supportSsbo) {
            ssboOffsetAlignment = GL11.glGetInteger(ARBShaderStorageBufferObject.GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
        } else {
            ssboOffsetAlignment = -1;
        }
        if (supportTextureBufferSlice) {
            textureBufferOffsetAlignment = GL11.glGetInteger(ARBTextureBufferRange.GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT);
        } else {
            textureBufferOffsetAlignment = -1;
        }

        glMajorVersion = GL11.glGetInteger(GL30C.GL_MAJOR_VERSION);
        glMinorVersion = GL11.glGetInteger(GL30C.GL_MINOR_VERSION);
    }

    @Inject(method = "compilePipeline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlProgram;setupUniforms(Ljava/util/List;Ljava/util/List;)V"))
    private void onSetGlProgram(RenderPipeline pipeline, BiFunction<ResourceLocation, ShaderType, String> sourceRetriever, CallbackInfoReturnable<GlRenderPipeline> cir, @Local GlProgram shaderProgram) {
        var shaderProgramExt = (ShaderProgramExtInternal) shaderProgram;
        var pipelineExt = (RenderPipelineExtInternal) pipeline;
        shaderProgramExt.blazerod$setStorageBuffers(pipelineExt.blazerod$getStorageBuffers());
    }

    @WrapOperation(method = "compileShader(Lcom/mojang/blaze3d/opengl/GlDevice$ShaderCompilationKey;Ljava/util/function/BiFunction;)Lcom/mojang/blaze3d/opengl/GlShaderModule;", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/preprocessor/GlslPreprocessor;injectDefines(Ljava/lang/String;Lnet/minecraft/client/renderer/ShaderDefines;)Ljava/lang/String;"))
    public String modifyShaderSource(String source, ShaderDefines defines, Operation<String> original) {
        var context = new GlslExtensionProcessor.Context(glMajorVersion, glMinorVersion, defines);
        var processedShader = GlslExtensionProcessor.process(context, source);
        return original.call(processedShader, defines);
    }

    @NotNull
    @Override
    public GpuBuffer blazerod$createBuffer(@Nullable Supplier<String> labelSupplier, int usage, int extraUsage, int size) {
        var buffer = createBuffer(labelSupplier, usage, size);
        ((GpuBufferExtInternal) buffer).blazerod$setExtraUsage(extraUsage);
        return buffer;
    }

    @NotNull
    @Override
    public GpuBuffer blazerod$createBuffer(@Nullable Supplier<String> labelSupplier, int usage, int extraUsage, ByteBuffer data) {
        var buffer = createBuffer(labelSupplier, usage, data);
        ((GpuBufferExtInternal) buffer).blazerod$setExtraUsage(extraUsage);
        return buffer;
    }

    @Override
    public boolean blazerod$supportTextureBufferSlice() {
        return supportTextureBufferSlice;
    }

    @Override
    public boolean blazerod$supportSsbo() {
        return supportSsbo;
    }

    @Override
    public boolean blazerod$supportComputeShader() {
        return supportComputeShader;
    }

    @Override
    public boolean blazerod$supportMemoryBarrier() {
        // glMemoryBarrier is defined in ARB_shader_image_load_store
        return supportShaderImageLoadStore;
    }

    @Override
    public boolean blazerod$supportShaderPacking() {
        return supportShaderPacking;
    }

    @Override
    public int blazerod$getMaxSsboBindings() {
        if (!supportSsbo) {
            throw new IllegalStateException("SSBO is not supported");
        }
        return maxSsboBindings;
    }

    @Override
    public int blazerod$getMaxSsboInVertexShader() {
        if (!supportSsbo) {
            throw new IllegalStateException("SSBO is not supported");
        }
        return maxSsboInVertexShader;
    }

    @Override
    public int blazerod$getMaxSsboInFragmentShader() {
        if (!supportSsbo) {
            throw new IllegalStateException("SSBO is not supported");
        }
        return maxSsboInFragmentShader;
    }

    @Override
    public int blazerod$getSsboOffsetAlignment() {
        if (!supportSsbo) {
            throw new IllegalStateException("SSBO is not supported");
        }
        return ssboOffsetAlignment;
    }

    @Override
    public int blazerod$getTextureBufferOffsetAlignment() {
        if (!supportTextureBufferSlice) {
            throw new IllegalStateException("Texture buffer slice is not supported");
        }
        return textureBufferOffsetAlignment;
    }

    @Override
    public CompiledComputePipeline blazerod$compilePipelineCached(ComputePipeline pipeline) {
        return this.computePipelineCompileCache.computeIfAbsent(pipeline, p -> this.compileComputePipeline(pipeline, defaultShaderSource));
    }

    @Unique
    private CompiledComputePipeline compileComputePipeline(ComputePipeline pipeline, BiFunction<ResourceLocation, ShaderType, String> sourceRetriever) {
        var compiledShader = getOrCompileShader(pipeline.getComputeShader(), ShaderTypeExt.COMPUTE, pipeline.getShaderDefines(), sourceRetriever);
        if (compiledShader == GlShaderModule.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: compute shader {} was invalid", pipeline.getLocation(), pipeline.getComputeShader());
            return new CompiledComputePipeline(pipeline, GlProgram.INVALID_PROGRAM);
        } else {
            com.mojang.blaze3d.opengl.GlProgram shaderProgram;
            try {
                shaderProgram = ShaderProgramExt.create(compiledShader, pipeline.getLocation().toString());
            } catch (net.minecraft.client.renderer.ShaderManager.CompilationException ex) {
                LOGGER.error("Couldn't compile program for pipeline {}: {}", pipeline.getLocation(), ex);
                return new CompiledComputePipeline(pipeline, GlProgram.INVALID_PROGRAM);
            }

            shaderProgram.setupUniforms(pipeline.getUniforms(), pipeline.getSamplers());
            ((ShaderProgramExtInternal) shaderProgram).blazerod$setStorageBuffers(pipeline.getStorageBuffers());
            debugLabels.applyLabel(shaderProgram);
            return new CompiledComputePipeline(pipeline, shaderProgram);
        }
    }
}
