package org.nakolotnik.wt.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.nakolotnik.wt.Watcher;

import java.util.ArrayList;
import java.util.List;

public class ChronoTerminalScreen extends Screen {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Watcher.MOD_ID, "textures/gui/chrono_terminal.png");
    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(Watcher.MOD_ID, "textures/gui/button_box.png");

    private static final int GUI_WIDTH = 350;
    private static final int GUI_HEIGHT = 200;

    private final List<UpgradeEntry> upgrades = new ArrayList<>();
    private int selectedTab = 0;
    private UpgradeEntry selectedUpgrade = null;
    private boolean mouseClicked = false;

    public ChronoTerminalScreen() {
        super(Component.translatable("gui.chrono_terminal.title"));
    }

    @Override
    protected void init() {
//        if (!TimeDetachManager.isDetached(Minecraft.getInstance().player.getUUID())) {
//            this.onClose();
//            return;
//        }

        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        addRenderableWidget(Button.builder(Component.literal("STABILIZATION"), b -> selectTab(0)).bounds(x + 10, y + 10, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CHRONO REACTIONS"), b -> selectTab(1)).bounds(x + 110, y + 10, 110, 20).build());
        addRenderableWidget(Button.builder(Component.literal("TRANSCENDENCE"), b -> selectTab(2)).bounds(x + 230, y + 10, 90, 20).build());

        addRenderableWidget(Button.builder(Component.literal("UPGRADE"), b -> upgrade()).bounds(x + 20, y + 145, 120, 20).build());

        populateUpgrades();
    }

    private void selectTab(int tab) {
        selectedTab = tab;
        selectedUpgrade = null;
        populateUpgrades();
    }

    private void upgrade() {
        if (selectedUpgrade != null && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Upgraded " + selectedUpgrade.name));
        }
    }

    private void populateUpgrades() {
        upgrades.clear();
        switch (selectedTab) {
            case 0 -> upgrades.add(new UpgradeEntry("Core Stabilizer", "Reduces time anomalies.", new ResourceLocation(Watcher.MOD_ID, "textures/gui/icons/core.png")));
            case 1 -> {
                upgrades.add(new UpgradeEntry("Accelerated Reflexes I", "Increases attack, mining, and movement speed.", new ResourceLocation(Watcher.MOD_ID, "textures/gui/icons/reflex1.png")));
                upgrades.add(new UpgradeEntry("Chrono-Regeneration", "Increases passive regeneration.", new ResourceLocation(Watcher.MOD_ID, "textures/gui/icons/regen.png")));
                upgrades.add(new UpgradeEntry("Phantom Absorption", "Immune to phantoms.", new ResourceLocation(Watcher.MOD_ID, "textures/gui/icons/phantom.png")));
            }
            case 2 -> upgrades.add(new UpgradeEntry("Temporal Transcendence", "No longer affected by hunger.", new ResourceLocation(Watcher.MOD_ID, "textures/gui/icons/transcend.png")));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        int leftX = x + 10;
        int rightX = x + 160;
        int yStart = y + 40;

        int offsetY = yStart;
        for (UpgradeEntry entry : upgrades) {
            boolean hovered = mouseX >= rightX && mouseX <= rightX + 140 && mouseY >= offsetY && mouseY <= offsetY + 22;

            guiGraphics.blit(BUTTON_TEXTURE, rightX, offsetY, 0, hovered ? 22 : 0, 140, 22);

            guiGraphics.blit(entry.icon, rightX + 5, offsetY + 3, 0, 0, 16, 16, 16, 16);
            guiGraphics.drawString(this.font, Component.literal(entry.name), rightX + 26, offsetY + 6, 0xB0FFFC);

            if (hovered && isMouseClicked()) {
                selectedUpgrade = entry;
            }

            offsetY += 24;
        }

        if (selectedUpgrade != null) {
            guiGraphics.blit(BUTTON_TEXTURE, leftX, yStart, 0, 44, 130, 90);
            guiGraphics.blit(selectedUpgrade.icon, leftX + 8, yStart + 8, 0, 0, 32, 32, 32, 32);

            int titleMaxWidth = 80;
            List<FormattedCharSequence> titleLines = this.font.split(Component.literal(selectedUpgrade.name), titleMaxWidth);
            int titleY = yStart + 8;
            for (int i = 0; i < Math.min(titleLines.size(), 2); i++) { // Limit to 2 lines for title
                guiGraphics.drawString(this.font, titleLines.get(i), leftX + 45, titleY, 0xFFFFFF);
                titleY += this.font.lineHeight;
            }

            int textX = leftX + 8;
            int textY = Math.max(yStart + 50, titleY + 5);
            int maxWidth = 114;

            int maxTextHeight = (yStart + 90) - textY - 10;

            List<FormattedCharSequence> lines = this.font.split(Component.literal(selectedUpgrade.description), maxWidth);
            int maxLines = maxTextHeight / this.font.lineHeight;

            for (int i = 0; i < Math.min(lines.size(), maxLines); i++) {
                guiGraphics.drawString(this.font, lines.get(i), textX, textY, 0xAAAAAA);
                textY += this.font.lineHeight;
            }

            guiGraphics.drawString(this.font, Component.literal("0/1"), leftX + 8,
                    yStart + 90 - this.font.lineHeight - 5, 0xAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseClicked = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseClicked() {
        boolean clicked = mouseClicked;
        mouseClicked = false;
        return clicked;
    }

    private record UpgradeEntry(String name, String description, ResourceLocation icon) {}
}
