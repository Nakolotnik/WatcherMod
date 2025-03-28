package org.nakolotnik.wt.events;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.Watcher;
import org.nakolotnik.wt.utils.ShaderHelper;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = Watcher.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventBusSubscriber {

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(Watcher.MOD_ID, "time_rift"),
                        DefaultVertexFormat.POSITION_COLOR),
                shader -> ShaderHelper.setRiftShader(shader)
        );

//        event.registerShader(
//                new ShaderInstance(event.getResourceProvider(),
//                        new ResourceLocation(Watcher.MOD_ID, "clock_shader"),
//                        DefaultVertexFormat.POSITION_COLOR),
//                shader -> ShaderHelper.setRiftShader(shader)
//        );
    }
}
