package org.nakolotnik.wt.init;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.entity.render.DarkEyesRenderer;
import org.nakolotnik.wt.entity.render.TimeRiftRenderer;
import org.nakolotnik.wt.entity.render.WatcherRenderer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityRenderers {
    public static void registerRenderers() {
        EntityRenderers.register(ModEntity.WATCHER, WatcherRenderer::new);
//        EntityRenderers.register(ModEntity.DARK_EYES, DarkEyesRenderer::new);
        EntityRenderers.register(ModEntity.TIME_RIFT, TimeRiftRenderer::new);
    }
}
