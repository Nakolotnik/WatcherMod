package org.nakolotnik.wt.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.entity.WathcerMob;
import org.nakolotnik.wt.init.ModEntity;
import org.nakolotnik.wt.init.ModSounds;

import java.util.*;

@Mod.EventBusSubscriber
public class WatcherSpawnHandler {
    private static final long SPAWN_INTERVAL = 10 * 60 * 20; // 10 минут в тиках
    private static final long WARNING_INTERVAL = 2 * 60 * 20; // 2 минуты до спавна (2400 тиков)
    private static final double SPAWN_CHANCE = 0.85;
    private static final int CHECK_RADIUS = 40; // Радиус проверки наличия мобов
    private static final int CLEAR_RADIUS = 200; // Радиус очистки старых мобов

    private static final Map<UUID, Long> lastSpawnTimes = new HashMap<>();
    private static final Set<UUID> warnedPlayers = new HashSet<>();
    private static final Set<UUID> effectAppliedPlayers = new HashSet<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            ServerLevel world = player.serverLevel();
            BlockPos playerPos = player.blockPosition();
            long currentTime = world.getGameTime();
            long lastSpawnTime = lastSpawnTimes.getOrDefault(playerId, 0L);
            long timeUntilSpawn = SPAWN_INTERVAL - (currentTime - lastSpawnTime);

            if (hasNearbyWatcher(world, playerPos)) continue;

            if (timeUntilSpawn <= WARNING_INTERVAL && timeUntilSpawn > WARNING_INTERVAL - 20 && !warnedPlayers.contains(playerId)) {
                player.sendSystemMessage(Component.translatable("message.watcher.warning"));
                warnedPlayers.add(playerId);
            }

            if (timeUntilSpawn <= 0 && canSpawnWatcher(player)) {
                if (player.getRandom().nextDouble() < SPAWN_CHANCE) {
                    removeAllWatchers(world, playerPos);
                    BlockPos spawnPos = playerPos.offset(3, 0, 3);
                    spawnWatcherAt(world, spawnPos);
                    lastSpawnTimes.put(playerId, currentTime);
                    warnedPlayers.remove(playerId);

                    if (!effectAppliedPlayers.contains(playerId)) {
                        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false, true));
                        effectAppliedPlayers.add(playerId);
                        world.getServer().execute(() -> effectAppliedPlayers.remove(playerId));
                    }

                    world.playSound(null, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                            ModSounds.WATCHER_DESPAWN_SOUND, net.minecraft.sounds.SoundSource.HOSTILE,
                            1.0F, 1.0F);
                }
            }
        }
    }

    private static boolean hasNearbyWatcher(ServerLevel world, BlockPos center) {
        return !world.getEntitiesOfClass(WathcerMob.class, new AABB(center).inflate(CHECK_RADIUS)).isEmpty();
    }


    private static boolean canSpawnWatcher(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        return world.getEntitiesOfClass(WathcerMob.class, new AABB(playerPos).inflate(CHECK_RADIUS)).isEmpty();
    }

    private static void removeAllWatchers(ServerLevel world, BlockPos pos) {
        List<WathcerMob> watchers = world.getEntitiesOfClass(WathcerMob.class, new AABB(pos).inflate(CLEAR_RADIUS));
        for (WathcerMob watcher : watchers) {
            if (!watcher.isRemoved()) watcher.discard();
        }
    }

    private static void spawnWatcherAt(ServerLevel world, BlockPos pos) {
        WathcerMob watcher = new WathcerMob(ModEntity.WATCHER, world);
        watcher.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.addFreshEntity(watcher);
    }
}