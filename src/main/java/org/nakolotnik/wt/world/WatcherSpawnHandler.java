package org.nakolotnik.wt.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.entity.WathcerMob;
import org.nakolotnik.wt.init.ModEntity;

import java.util.*;

@Mod.EventBusSubscriber
public class WatcherSpawnHandler {
    private static final long SPAWN_INTERVAL = 10 * 60 * 20; // 600 секунд в тиках
    private static final double SPAWN_CHANCE = 1;
    private static final int SPAWN_RADIUS = 10;
    private static final int MAX_WATCHERS = 1;
    private static final int CHECK_RADIUS = 40;
    private static final int CLEAR_RADIUS = 200;

    private static final Map<UUID, Long> lastSpawnTimes = new HashMap<>();
    private static final Set<UUID> pausedPlayers = new HashSet<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            ServerLevel world = player.serverLevel();
            BlockPos playerPos = player.blockPosition();
            long currentTime = world.getGameTime();

            // Если игрок только зашёл, устанавливаем ему начальное время спавна
            lastSpawnTimes.putIfAbsent(playerId, currentTime);

            long lastSpawnTime = lastSpawnTimes.get(playerId);
            long timeLeft = SPAWN_INTERVAL - (currentTime - lastSpawnTime);

            boolean hasWatcherNearby = isWatcherNearby(world, playerPos);

            if (hasWatcherNearby) {
                pausedPlayers.add(playerId);
                continue;
            } else {
                pausedPlayers.remove(playerId);
            }

            if (timeLeft <= 0 && canSpawnWatcher(player)) {
                if (player.getRandom().nextDouble() < SPAWN_CHANCE) {
                    removeAllWatchers(world, playerPos);
                    BlockPos spawnPos = spawnWatcherAtRandomLocation(player);
                    lastSpawnTimes.put(playerId, currentTime);
                }
            }
        }
    }

    private static boolean isWatcherNearby(ServerLevel world, BlockPos pos) {
        return !world.getEntitiesOfClass(WathcerMob.class, new AABB(pos).inflate(CHECK_RADIUS)).isEmpty();
    }

    private static boolean canSpawnWatcher(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        return world.getEntitiesOfClass(WathcerMob.class, new AABB(playerPos).inflate(CHECK_RADIUS)).size() < MAX_WATCHERS;
    }

    private static void removeAllWatchers(ServerLevel world, BlockPos pos) {
        List<WathcerMob> watchers = world.getEntitiesOfClass(WathcerMob.class, new AABB(pos).inflate(CLEAR_RADIUS));

        if (!watchers.isEmpty()) {
            for (WathcerMob watcher : watchers) {
                if (!watcher.isRemoved()) {
                    watcher.discard();
                }
            }
        }
    }

    private static BlockPos spawnWatcherAtRandomLocation(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos randomPos = findRandomNearbyPosition(world, player.blockPosition(), SPAWN_RADIUS);

        if (randomPos != null) {
            spawnWatcherAt(world, randomPos);
            return randomPos;
        } else {
            return null;
        }
    }

    private static void spawnWatcherAt(ServerLevel world, BlockPos pos) {
        WathcerMob watcher = new WathcerMob(ModEntity.WATCHER, world);
        watcher.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.addFreshEntity(watcher);
    }

    private static BlockPos findRandomNearbyPosition(ServerLevel world, BlockPos basePos, int radius) {
        RandomSource random = RandomSource.create();

        for (int i = 0; i < 20; i++) {
            BlockPos potentialPos = basePos.offset(random.nextInt(radius * 2 + 1) - radius, 0, random.nextInt(radius * 2 + 1) - radius);

            if (isPositionSafe(world, potentialPos)) {
                return potentialPos;
            }
        }
        return null;
    }

    private static boolean isPositionSafe(ServerLevel world, BlockPos pos) {
        return world.getBlockState(pos).isAir() && world.getBlockState(pos.above()).isAir();
    }

    public static boolean isSpawnPaused(UUID playerId) {
        return pausedPlayers.contains(playerId);
    }

    public static double getSpawnChance() {
        return SPAWN_CHANCE;
    }

    public static long getTimeUntilSpawn(UUID playerId, Level world) {
        if (world.isClientSide()) return -1;
        long currentTime = world.getGameTime();

        lastSpawnTimes.putIfAbsent(playerId, currentTime);

        long lastSpawnTime = lastSpawnTimes.get(playerId);
        long timeLeft = SPAWN_INTERVAL - (currentTime - lastSpawnTime);

        return pausedPlayers.contains(playerId) ? -1 : Math.max(0, timeLeft);
    }
}
