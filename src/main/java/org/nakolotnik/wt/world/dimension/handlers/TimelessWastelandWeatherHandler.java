package org.nakolotnik.wt.world.dimension.handlers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TimelessWastelandWeatherHandler {

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ResourceLocation dimension = serverLevel.dimension().location();
            if (dimension.equals(new ResourceLocation("wt:timeless_wasteland"))) {
                serverLevel.setWeatherParameters(0, 0, false, false);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.level instanceof ServerLevel serverLevel) {
            ResourceLocation dimension = serverLevel.dimension().location();
            if (dimension.equals(new ResourceLocation("wt:timeless_wasteland"))) {
                serverLevel.setWeatherParameters(0, 0, false, false);
            }
        }
    }
}