package org.nakolotnik.wt.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.nakolotnik.wt.client.overlay.WatcherMessageOverlayManager;

import java.util.function.Supplier;

public class ShowWatcherMessagePacket {
    private final int entityId;

    public ShowWatcherMessagePacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(entityId);
                if (entity != null) {
                    WatcherMessageOverlayManager.show(entity);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
