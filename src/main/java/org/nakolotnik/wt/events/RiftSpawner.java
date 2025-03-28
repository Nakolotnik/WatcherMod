package org.nakolotnik.wt.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nakolotnik.wt.entity.TimeRiftEntity;
import org.nakolotnik.wt.init.ModEntity;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "wt", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RiftSpawner {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int SPAWN_CHECK_INTERVAL = 200; // Проверять каждые 10 секунд (20 тиков/сек)
    private static final double SPAWN_CHANCE = 0.15; // 15% шанс спавна за проверку
    private static final double SPAWN_RADIUS = 100.0; // Радиус поиска игроков
    private static final double SPAWN_DISTANCE_MIN = 30.0; // Минимальная дистанция от игрока
    private static final double SPAWN_DISTANCE_MAX = 80.0; // Максимальная дистанция от игрока
    private static final int MAX_RIFTS_PER_PLAYER = 3; // Максимум разломов на игрока
    private static final int HEIGHT_OFFSET = 5; // Смещение на 5 блоков над землёй

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < SPAWN_CHECK_INTERVAL) return;
        tickCounter = 0;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.dimension() != Level.OVERWORLD) continue;

            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) continue;

            for (Player player : players) {
                AABB checkArea = new AABB(
                        player.getX() - SPAWN_RADIUS, player.getY() - SPAWN_RADIUS, player.getZ() - SPAWN_RADIUS,
                        player.getX() + SPAWN_RADIUS, player.getY() + SPAWN_RADIUS, player.getZ() + SPAWN_RADIUS
                );
                List<TimeRiftEntity> nearbyRifts = level.getEntitiesOfClass(TimeRiftEntity.class, checkArea);
                if (nearbyRifts.size() >= MAX_RIFTS_PER_PLAYER) continue;

                Random rand = new Random();
                if (rand.nextDouble() > SPAWN_CHANCE) continue;

                double angle = rand.nextDouble() * Math.PI * 2;
                double distance = SPAWN_DISTANCE_MIN + rand.nextDouble() * (SPAWN_DISTANCE_MAX - SPAWN_DISTANCE_MIN);
                double spawnX = player.getX() + Math.cos(angle) * distance;
                double spawnZ = player.getZ() + Math.sin(angle) * distance;

                BlockPos pos = new BlockPos((int)spawnX, (int)player.getY(), (int)spawnZ);
                String biomeName = level.getBiome(pos).toString().toLowerCase();

                int spawnY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY();
                if (spawnY < level.getMinBuildHeight() || spawnY > level.getMaxBuildHeight() - HEIGHT_OFFSET) continue;

                // Добавляем смещение на 5 блоков выше поверхности
                double finalSpawnY = spawnY + HEIGHT_OFFSET;

                TimeRiftEntity rift = new TimeRiftEntity(ModEntity.TIME_RIFT, level);
                rift.setPos(spawnX, finalSpawnY, spawnZ);
                level.addFreshEntity(rift);

                LOGGER.info("Time Rift spawned at [x={}, y={}, z={}] in biome {} near player {} at [x={}, y={}, z={}]",
                        spawnX, finalSpawnY, spawnZ, biomeName, player.getName().getString(),
                        player.getX(), player.getY(), player.getZ());

                for (int i = 0; i < 20; i++) {
                    level.addParticle(
                            net.minecraft.core.particles.ParticleTypes.PORTAL,
                            spawnX + (rand.nextDouble() - 0.5) * 5.0,
                            finalSpawnY + (rand.nextDouble() - 0.5) * 5.5,
                            spawnZ + (rand.nextDouble() - 0.5) * 5.0,
                            0.0, 0.0, 0.0
                    );
                }
                level.playSound(null, spawnX, finalSpawnY, spawnZ,
                        net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.AMBIENT,
                        1.0f, 1.0f);
            }
        }
    }
}