package top.fifthlight.blazerod.extension.internal;

import com.mojang.blaze3d.opengl.GlDevice;
import top.fifthlight.blazerod.extension.CommandEncoderExt;
import top.fifthlight.blazerod.systems.ComputePass;

public interface CommandEncoderExtInternal extends CommandEncoderExt {
    GlDevice blazerod$getDevice();

    void blazerod$dispatchCompute(ComputePass pass, int x, int y, int z);
}
