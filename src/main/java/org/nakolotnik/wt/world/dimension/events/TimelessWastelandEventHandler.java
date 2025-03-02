package org.nakolotnik.wt.world.dimension.events;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.client.overlay.WatcherMessageOverlayManager;
import org.nakolotnik.wt.entity.WathcerMob;
import org.nakolotnik.wt.init.ModEntity;
import org.nakolotnik.wt.init.WatcherMessageRegistry;
import org.nakolotnik.wt.utils.DimensionChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class TimelessWastelandEventHandler {

    private static final Map<UUID, Long> playerEntryTimes = new HashMap<>();
    private static final long DELAY_TICKS = 10 * 20;  // 10 секунд
    private static final long PARALYSIS_TICKS = 40;   // 2 секунды паралича
    private static final long EXTRA_DELAY_TICKS = 60; // Дополнительные 3 секунды перед телепортом

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        if (DimensionChecker.isInTimelessWasteland(player)) {
            playerEntryTimes.put(player.getUUID(), player.serverLevel().getGameTime());
        }
    }

    @SubscribeEvent
    public static void onEnterDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (DimensionChecker.isInTimelessWasteland(player)) {
            playerEntryTimes.put(player.getUUID(), player.serverLevel().getGameTime());
            grantAchievement(player);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!DimensionChecker.isInTimelessWasteland(player)) continue;

            UUID playerId = player.getUUID();
            ServerLevel world = player.serverLevel();
            playerEntryTimes.putIfAbsent(playerId, world.getGameTime());

            long entryTime = playerEntryTimes.get(playerId);
            long currentTime = world.getGameTime();
            long timeElapsed = currentTime - entryTime;

            if (timeElapsed >= DELAY_TICKS - PARALYSIS_TICKS) {
                applyParalysis(player);
            }

            if (timeElapsed == DELAY_TICKS) {
                summonWatcher(player);
            }

            if (timeElapsed >= DELAY_TICKS && timeElapsed < DELAY_TICKS + EXTRA_DELAY_TICKS) {
                WathcerMob watcher = findExistingWatcher(world, player);
                if (watcher != null) {
                    forcePlayerLookAt(player, watcher);
                }
            }

            if (timeElapsed >= DELAY_TICKS + EXTRA_DELAY_TICKS) {
                playerEntryTimes.remove(playerId);
                teleportPlayerBack(player);
            }
        }
    }

    private static void applyParalysis(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
        player.setJumping(false);
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    private static void restoreMovement(ServerPlayer player) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1);
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
    }

    private static void summonWatcher(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        WathcerMob watcher = findExistingWatcher(world, player);

        BlockPos frontPos = player.blockPosition().offset(0, 0, -2);

        if (watcher == null) {
            watcher = new WathcerMob(ModEntity.WATCHER, world);
            watcher.setPos(frontPos.getX() + 0.5, frontPos.getY(), frontPos.getZ() + 0.5);
            world.addFreshEntity(watcher);
        } else {
            watcher.teleportTo(frontPos.getX() + 0.5, frontPos.getY(), frontPos.getZ() + 0.5);
        }

        forcePlayerLookAt(player, watcher);
        WatcherMessageOverlayManager.showWatcherMessage(watcher, WatcherMessageRegistry.getWatcherMessage());
        spawnSmokeEffect(player);
    }

    private static WathcerMob findExistingWatcher(ServerLevel world, ServerPlayer player) {
        List<WathcerMob> watchers = world.getEntitiesOfClass(WathcerMob.class, new AABB(player.blockPosition()).inflate(10));
        return watchers.isEmpty() ? null : watchers.get(0);
    }

    private static void spawnSmokeEffect(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos pos = player.blockPosition();

        for (int i = 0; i < 20; i++) {
            world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    10, 0.2, 0.5, 0.2, 0.02);
        }
    }

    public static void teleportPlayerBack(ServerPlayer player) {
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            BlockPos safePos = findSafeTeleportLocation(overworld, player.blockPosition());
            player.teleportTo(overworld, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, player.getYRot(), player.getXRot());
        }
        restoreMovement(player);
    }

    private static BlockPos findSafeTeleportLocation(ServerLevel world, BlockPos playerPos) {
        int x = playerPos.getX();
        int z = playerPos.getZ();

        for (int y = world.getMaxBuildHeight(); y > world.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(x, y, z);
            BlockState blockBelow = world.getBlockState(checkPos);
            BlockState blockAbove = world.getBlockState(checkPos.above());

            if (!blockBelow.isAir() && blockAbove.isAir() && !blockBelow.is(Blocks.WATER)) {
                return checkPos.above();
            }
        }

        return new BlockPos(x, 80, z);
    }

    private static void forcePlayerLookAt(ServerPlayer player, WathcerMob watcher) {
        double x = watcher.getX();
        double y = watcher.getEyeY();
        double z = watcher.getZ();

        player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(x, y, z));

        float yaw = player.getYHeadRot();
        float pitch = player.getXRot();
        player.setYRot(yaw);
        player.setXRot(pitch);

        player.connection.send(new ClientboundRotateHeadPacket(player, (byte) (yaw * 256 / 360)));
    }

    private static void grantAchievement(ServerPlayer player) {
        Advancement advancement = player.getServer().getAdvancements()
                .getAdvancement(new ResourceLocation("wt:timeless_wasteland_achievement"));
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }
}
