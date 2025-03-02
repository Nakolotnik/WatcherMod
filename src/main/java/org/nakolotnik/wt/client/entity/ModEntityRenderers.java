package org.nakolotnik.wt.client.entity;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.entity.render.WatcherRenderer;
import org.nakolotnik.wt.init.ModEntity;



@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityRenderers {
    public static void registerRenderers() {
        EntityRenderers.register(ModEntity.WATCHER, WatcherRenderer::new);
    }
}