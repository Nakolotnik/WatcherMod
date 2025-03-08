package org.nakolotnik.wt.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.nakolotnik.wt.network.ShowWatcherMessagePacket;

public class ModPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("wt", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, ShowWatcherMessagePacket.class, ShowWatcherMessagePacket::encode, ShowWatcherMessagePacket::new, ShowWatcherMessagePacket::handle);
    }
}
