package org.nakolotnik.wt.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.client.overlay.WatcherMessageOverlayManager;
import org.nakolotnik.wt.entity.WathcerMob;

@Mod.EventBusSubscriber
public class WatcherSilenceEvent {
    private static final int EFFECT_RADIUS = 5;

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        if (isNearWatcher(event.getPlayer())) {
            event.setMessage(Component.literal("...."));
        }
    }


    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player) {
            if (isNearWatcher(player)) {
                event.setCanceled(true);
                WatcherMessageOverlayManager.showStaticMessage(player, "[Ни богов, ни господ]");
            }
        }
    }


    private static boolean isNearWatcher(Player player) {
        Level world = player.level();
        return world.getEntitiesOfClass(WathcerMob.class, new AABB(player.blockPosition()).inflate(EFFECT_RADIUS)).size() > 0;
    }
}
