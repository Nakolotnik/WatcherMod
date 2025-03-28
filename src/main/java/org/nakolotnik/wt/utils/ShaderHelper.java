package org.nakolotnik.wt.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.nakolotnik.wt.Watcher;

public final class ShaderHelper {

    public static final ResourceLocation RIFT_TEXTURE = new ResourceLocation(Watcher.MOD_ID, "textures/block/rift.png");
//    public static final ResourceLocation CLOCK_TEXTURE = new ResourceLocation(Watcher.MOD_ID, "textures/misc/clock.png");

    private static ShaderInstance riftShader;
//    private static ShaderInstance clockShader;

    public static final RenderType RIFT = RenderType.create(
            "rift",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.TRIANGLES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(ShaderHelper::getRiftShader))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                            "translucent_transparency",
                            () -> {
                                RenderSystem.enableBlend();
                                RenderSystem.defaultBlendFunc();
                            },
                            () -> {
                                RenderSystem.disableBlend();
                                RenderSystem.defaultBlendFunc();
                            }
                    ))
                    .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                            .add(RIFT_TEXTURE, false, false).build())
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .createCompositeState(false)
    );

    public static ShaderInstance getRiftShader() {
        return riftShader;
    }

    public static void setRiftShader(ShaderInstance shader) {
        riftShader = shader;
    }



//    public static final RenderType CLOCK = RenderType.create(
//            "clock",
//            DefaultVertexFormat.POSITION_COLOR_TEX,
//            VertexFormat.Mode.TRIANGLES,
//            256,
//            false,
//            false,
//            RenderType.CompositeState.builder()
//                    .setShaderState(new RenderStateShard.ShaderStateShard(ShaderHelper::getClockShader))
//                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
//                            "translucent_transparency",
//                            () -> {
//                                RenderSystem.enableBlend();
//                                RenderSystem.defaultBlendFunc();
//                            },
//                            () -> {
//                                RenderSystem.disableBlend();
//                                RenderSystem.defaultBlendFunc();
//                            }
//                    ))
//                    .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
//                            .add(CLOCK_TEXTURE, false, false).build())
//                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
//                    .setCullState(new RenderStateShard.CullStateShard(false))
//                    .createCompositeState(false)
//    );
//
//
//
//    public static ShaderInstance getClockShader() {
//        return clockShader;
//    }
//
//    public static void setClockShader(ShaderInstance shader) {
//        clockShader = shader;
//    }
}