package org.nakolotnik.wt.client.overlay;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class WatcherMessageOverlay {
    private final long displayTime;
    private final long startTime;
    private final Component selectedMessage;
    private final Entity watcherEntity;

    public WatcherMessageOverlay(Component message, Entity entity) {
        this.displayTime = 4 * 1000;
        this.startTime = System.currentTimeMillis();
        this.selectedMessage = message;
        this.watcherEntity = entity;
    }

    public boolean shouldRemove() {
        return System.currentTimeMillis() - startTime > displayTime;
    }

    public Component getSelectedMessage() {
        return selectedMessage;
    }


}
