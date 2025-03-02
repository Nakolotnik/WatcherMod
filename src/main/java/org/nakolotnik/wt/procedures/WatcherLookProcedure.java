package org.nakolotnik.wt.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.nakolotnik.wt.client.overlay.WatcherMessageOverlayManager;
import org.nakolotnik.wt.init.ModSounds;
import org.nakolotnik.wt.init.WatcherMessageRegistry;
import org.nakolotnik.wt.utils.DimensionChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WatcherLookProcedure extends DimensionChecker {
    private static final Map<UUID, Long> playerLookTimes = new HashMap<>();

    public static void execute(Entity entity, Player player) {
        if (entity == null || player == null) return;
        if (isInTimelessWasteland(entity)) return;

        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();

        if (player.getLookAngle().dot(entity.position().subtract(player.position()).normalize()) > 0.98) {
            if (!playerLookTimes.containsKey(playerId)) {
                playerLookTimes.put(playerId, currentTime);
            }

            long lookDuration = currentTime - playerLookTimes.get(playerId);

            if (lookDuration > 3000) {
                if (player instanceof ServerPlayer serverPlayer) {

                    serverPlayer.playNotifySound(ModSounds.WHISP_AMBIENT.get(), entity.getSoundSource(), 0.2f, 1.0f);

                    if (WatcherMessageRegistry.canSendMessage(serverPlayer)) {
                        WatcherMessageOverlayManager.show(entity);
                    }
                }
                playerLookTimes.remove(playerId);
            }
        } else {
            playerLookTimes.remove(playerId);
        }
    }
}
