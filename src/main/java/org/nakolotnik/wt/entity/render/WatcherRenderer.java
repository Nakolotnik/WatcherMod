package org.nakolotnik.wt.entity.render;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.nakolotnik.wt.entity.WathcerMob;
import org.nakolotnik.wt.init.ModEntity;
import org.nakolotnik.wt.init.ModGeometries;
import org.zeith.hammeranims.api.HammerAnimationsApi;
import org.zeith.hammeranims.api.geometry.event.RefreshStaleModelsEvent;
import org.zeith.hammeranims.api.geometry.model.IGeometricModel;
import org.zeith.hammeranims.core.client.render.entity.BedrockEntityRenderer;
import org.zeith.hammeranims.core.client.render.entity.BedrockModelWrapper;
import org.zeith.hammeranims.core.client.render.entity.proc.HeadLookProcessor;


import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WatcherRenderer extends BedrockEntityRenderer<WathcerMob> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("wt", "textures/entity/watcher.png");
    private static final ResourceLocation EMISSION_TEXTURE = new ResourceLocation("wt", "textures/entity/watcher_emission.png");

    private IGeometricModel watcherModel;

    public WatcherRenderer(EntityRendererProvider.Context context) {
        super(context, ModGeometries.WATCHER, 0.5F);
        this.watcherModel = ModGeometries.WATCHER.createModel();
        HammerAnimationsApi.EVENT_BUS.addListener(this::refreshModel);
    }

    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntity.WATCHER, WatcherRenderer::new);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(WathcerMob entity) {
        return TEXTURE;
    }

    @Override
    protected void addProcessors(BedrockModelWrapper<WathcerMob> model) {
        model.addProcessor(new HeadLookProcessor("Head"));
    }


    @Override
    protected List<RenderType> getRenderPasses(WathcerMob entity) {
        return List.of(
                RenderType.entityTranslucent(TEXTURE),
                RenderType.eyes(EMISSION_TEXTURE)
        );
    }

    public void refreshModel(RefreshStaleModelsEvent e) {
        this.watcherModel = ModGeometries.WATCHER.createModel();
    }

    @Override
    protected boolean shouldShowName(WathcerMob entity) {
        return false;
    }


}
