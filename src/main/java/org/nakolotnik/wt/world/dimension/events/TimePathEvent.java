package org.nakolotnik.wt.world.dimension.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.init.ModBlocks;

import java.util.*;

@Mod.EventBusSubscriber
public class TimePathEvent {
    private static final Random random = new Random();
    private static final Map<UUID, List<BlockPos>> playerPaths = new HashMap<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new TimePathEvent());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player && event.phase == TickEvent.Phase.END) {
            ServerLevel level = (ServerLevel) player.level();

            if (playerPaths.containsKey(player.getUUID())) {
                List<BlockPos> path = playerPaths.get(player.getUUID());

                if (!path.isEmpty() && path.contains(player.blockPosition())) {
                    path.remove(player.blockPosition());
                    if (path.isEmpty()) {
                        teleportPlayerToRandomWorld(player);
                    }
                }
                return;
            }

            if (level.dimension().location().toString().contains("timeless_wasteland") && random.nextFloat() < 0.1f) {
                generatePathForPlayer(player);
            }
        }
    }

    public static void generatePathForPlayer(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos start = player.blockPosition();
        int length = random.nextInt(21) + 15;
        int direction = random.nextBoolean() ? 1 : -1;
        List<BlockPos> path = new ArrayList<>();

        BlockPos endZone;
        int centerZ = start.getZ();

        for (int i = 0; i < length; i++) {
            int x = start.getX() + i * direction;
            int y = findGround(level, x, start.getY(), centerZ);

            for (int w = -2; w <= 2; w++) {
                BlockPos pos = new BlockPos(x, y, centerZ + w);
                path.add(pos);
                spawnCampfireParticles(level, pos);
            }
        }

        playerPaths.put(player.getUUID(), path);

        endZone = path.get(path.size() - 1).offset(direction, 0, 0);
        createTeleportZone(level, endZone, centerZ);

        String logMessage = String.format(
                "§6Зона пробежки: §aНачало [%d, %d, %d] §b→ Конец [%d, %d, %d]",
                start.getX(), start.getY(), start.getZ(),
                endZone.getX(), endZone.getY(), endZone.getZ()
        );

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(logMessage));
        System.out.println("[TimePath] " + logMessage);
    }

    private static int findGround(ServerLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        while (level.getBlockState(pos).isAir() && pos.getY() > level.getMinBuildHeight()) {
            pos = pos.below();
        }
        return pos.getY() + 1;
    }

    private static void spawnCampfireParticles(ServerLevel level, BlockPos pos) {
        level.getServer().execute(() -> level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                3, 0.1, 0.1, 0.1, 0.01
        ));
    }

    private static void createTeleportZone(ServerLevel level, BlockPos endPos, int centerZ) {
        int centerX = endPos.getX();
        int centerY = endPos.getY();

        for (int y = 0; y < 2; y++) {
            for (int x = -2; x <= 2; x++) {
                BlockPos triggerPos = findSafeTriggerPosition(level, centerX, centerY + y, centerZ + x);
                level.setBlock(triggerPos, ModBlocks.WT_BLOCK_TELEPORT.defaultBlockState(), 3);
            }
        }
    }

    private static BlockPos findSafeTriggerPosition(ServerLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);

        if (!state.isAir() && !state.canBeReplaced()) {
            pos = pos.above();
        }
        return pos;
    }

    public static void teleportPlayerToRandomWorld(ServerPlayer player) {
        List<ServerLevel> worlds = new ArrayList<>();
        player.getServer().getAllLevels().forEach(worlds::add);

        if (worlds.isEmpty()) return;

        ServerLevel targetWorld = worlds.get(random.nextInt(worlds.size()));
        BlockPos safePos = findSafeTeleportLocation(targetWorld);

        player.teleportTo(targetWorld, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, player.getYRot(), player.getXRot());
        playerPaths.remove(player.getUUID());
    }

    private static BlockPos findSafeTeleportLocation(ServerLevel level) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        if (level.dimensionType().hasCeiling()) {
            maxY = Math.min(maxY, 127);
        }

        for (int attempt = 0; attempt < 50; attempt++) {
            int x = random.nextInt(10000) - 5000;
            int z = random.nextInt(10000) - 5000;

            int middleY = (minY + maxY) / 2;

            int y = findGround(level, x, middleY, z);

            if (y < minY || y > maxY) continue;

            BlockPos pos = new BlockPos(x, y, z);

            if (isSafeLocation(level, pos)) {
                return pos;
            }
        }

        return level.getSharedSpawnPos();
    }


    private static boolean isSafeLocation(ServerLevel level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState at = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());

        return below.isSolid() && at.isAir() && above.isAir();
    }
}
