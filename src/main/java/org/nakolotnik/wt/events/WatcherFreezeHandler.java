package org.nakolotnik.wt.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.entity.WathcerMob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class WatcherFreezeHandler {
    private static final int DISTANCE_CHECK = 10;
    private static final int MAX_FREEZE_LEVEL = 3;
    private static final long FREEZE_INCREMENT_TICKS = 20; // Каждые 20 тиков (~1 сек) уровень заморозки увеличивается
    private static final long REQUIRED_LOOK_TIME = 140; // 7 секунд (20 * 7)
    private static final int POWDER_SNOW_FREEZING_THRESHOLD = 140;

    private static final Map<UUID, Long> playerFreezeStart = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        Level world = player.level();
        UUID playerId = player.getUUID();

        boolean isLookingAtWatcher = false;
        for (WathcerMob watcher : world.getEntitiesOfClass(WathcerMob.class, new AABB(player.blockPosition()).inflate(DISTANCE_CHECK))) {
            if (isPlayerLookingAt(player, watcher)) {
                isLookingAtWatcher = true;
                break;
            }
        }

        if (isLookingAtWatcher) {
            if (!playerFreezeStart.containsKey(playerId)) {
                playerFreezeStart.put(playerId, world.getGameTime()); // Начинаем отсчёт, если игрок только начал смотреть
            }

            long timeLooking = world.getGameTime() - playerFreezeStart.get(playerId);

            if (timeLooking >= REQUIRED_LOOK_TIME) { // Только если прошло 7 секунд
                int freezeLevel = Math.min((int) ((timeLooking - REQUIRED_LOOK_TIME) / FREEZE_INCREMENT_TICKS), MAX_FREEZE_LEVEL);

                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, freezeLevel, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, freezeLevel, true, false, true));

                if (player instanceof ServerPlayer serverPlayer) {
                    if (serverPlayer.getTicksFrozen() < POWDER_SNOW_FREEZING_THRESHOLD) {
                        serverPlayer.setTicksFrozen(serverPlayer.getTicksFrozen() + 4);
                    }
                }
            }
        } else {
            playerFreezeStart.remove(playerId);
        }
    }

    private static boolean isPlayerLookingAt(Player player, WathcerMob watcher) {
        return player.getLookAngle().dot(watcher.position().subtract(player.position()).normalize()) > 0.98;
    }
}
