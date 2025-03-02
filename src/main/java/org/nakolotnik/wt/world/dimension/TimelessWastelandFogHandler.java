package org.nakolotnik.wt.world.dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimelessWastelandFogHandler {

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Level level = mc.level;
        ResourceLocation dimension = level.dimension().location();

        if (dimension.equals(new ResourceLocation("wt:timeless_wasteland"))) {
            event.setCanceled(true);
            FogRenderer.FogMode mode = event.getMode();
            float nearFog = 3.0f;
            float farFog = 80.0f;

            if (mode == FogRenderer.FogMode.FOG_TERRAIN || mode == FogRenderer.FogMode.FOG_SKY) {
                event.setNearPlaneDistance(nearFog);
                event.setFarPlaneDistance(farFog);
            }
        }
    }
}
