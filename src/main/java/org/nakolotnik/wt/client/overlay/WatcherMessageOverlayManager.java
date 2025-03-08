package org.nakolotnik.wt.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.init.WatcherMessageRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber
public class WatcherMessageOverlayManager {
    private static final ResourceLocation ICON = new ResourceLocation("wt", "textures/gui/watcher_icon.png");
    private static final List<WatcherMessageOverlay> activeMessages = new ArrayList<>();

    private static final int backgroundTransparency = 95;
    private static final Component WATCHER_NAME = Component.literal("[Смотрящий]: ").withStyle(style -> style.withColor(0xFFFFFF));

    public static void show(Entity entity) {
        activeMessages.clear();
        activeMessages.add(new WatcherMessageOverlay(WatcherMessageRegistry.getRandomMessage(), entity));
    }

    public static void showStaticMessage(Entity entity, String message) {
        activeMessages.clear();
        activeMessages.add(new WatcherMessageOverlay(Component.literal(message).withStyle(style -> style.withColor(0xAAAAAA)), entity));
    }

    public static void showWatcherMessage(Entity entity, Component message) {
        activeMessages.clear();
        activeMessages.add(new WatcherMessageOverlay(message, entity));
    }


    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int inventoryY = height - 50;

        Iterator<WatcherMessageOverlay> iterator = activeMessages.iterator();
        while (iterator.hasNext()) {
            WatcherMessageOverlay overlay = iterator.next();

            if (overlay.shouldRemove()) {
                iterator.remove();
                continue;
            }

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int textWidth = mc.font.width(overlay.getSelectedMessage());
            int textHeight = mc.font.lineHeight;
            int totalWidth = textWidth + 30; // Учёт иконки
            int textX = (width - totalWidth) / 2 + 24; // Смещаем немного левее
            int textY = inventoryY - textHeight / 2;

            int iconSize = 16;
            int iconX = textX - 20;
            int iconY = textY + (textHeight - iconSize) / 2;

            int backgroundX = iconX - 5;
            int backgroundY = iconY - 2;
            int backgroundWidth = totalWidth + 10;
            int backgroundHeight = Math.max(iconSize, textHeight) + 4;

            int alpha = (int) ((1.0f - (backgroundTransparency / 100.0f)) * 255);
            int bgColor = (alpha << 24);

            guiGraphics.fill(backgroundX, backgroundY, backgroundX + backgroundWidth, backgroundY + backgroundHeight, bgColor);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, ICON);

            guiGraphics.blit(ICON, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            guiGraphics.drawString(mc.font, overlay.getSelectedMessage(), textX, textY, 0xAAAAAA);

            RenderSystem.disableBlend();
        }
    }
}
