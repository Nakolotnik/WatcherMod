package org.nakolotnik.wt.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import org.nakolotnik.wt.init.ModItems;
import org.nakolotnik.wt.init.ModSounds;
import org.nakolotnik.wt.utils.DimensionChecker;

import java.util.ArrayList;
import java.util.List;

public class WatcherIdleProcedure {

    private static final double SOUND_CHANCE = 1.0;
    private static final double DEFAULT_DESPAWN_RADIUS = 3.0;
    private static final double WT_DESPAWN_RADIUS = 1.0;

    private static final List<DropEntry> DROP_TABLE = new ArrayList<>();

    static {
        DROP_TABLE.add(new DropEntry(ModItems.TIME_SHARD, 1.00f, 1, 5));
    }

    private static class DropEntry {
        final Item item;
        final float chance;
        final int minCount;
        final int maxCount;

        DropEntry(Item item, float chance, int minCount, int maxCount) {
            this.item = item;
            this.chance = chance;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
    }

    public static void execute(Entity entity) {
        if (entity == null || !(entity.level() instanceof ServerLevel)) return;

        ServerLevel world = (ServerLevel) entity.level();
        CompoundTag data = entity.getPersistentData();

        world.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                entity.getX(), entity.getY() + 0.5, entity.getZ(),
                5,
                0.0005, 0.02, 0.5,
                0.005
        );

        double despawnRadius = !DimensionChecker.isInTimelessWasteland(entity) ? DEFAULT_DESPAWN_RADIUS : WT_DESPAWN_RADIUS;

        for (Player player : world.players()) {
            if (player.distanceTo(entity) < despawnRadius) {
                if (!data.contains("origX")) {
                    data.putDouble("origX", entity.getX());
                    data.putDouble("origY", entity.getY());
                    data.putDouble("origZ", entity.getZ());
                    data.putInt("teleportCount", 0);
                }

                int count = data.getInt("teleportCount") + 1;
                data.putInt("teleportCount", count);

                world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        entity.getX(), entity.getY(), entity.getZ(),
                        50, 0.1, 0.1, 0.1, 0.2);

                RandomSource random = player.getRandom();
                if (random.nextDouble() < SOUND_CHANCE) {
                    world.playSound(null, entity.blockPosition(), ModSounds.WATCHER_DESPAWN_SOUND, SoundSource.HOSTILE, 1.0f, 1.0f);
                }

                for (DropEntry entry : DROP_TABLE) {
                    if (random.nextFloat() < entry.chance) {
                        int dropCount = entry.minCount + random.nextInt(entry.maxCount - entry.minCount + 1);
                        ItemStack itemStack = new ItemStack(entry.item, dropCount);
                        ItemEntity itemEntity = new ItemEntity(
                                world,
                                entity.getX(),
                                entity.getY(),
                                entity.getZ(),
                                itemStack
                        );
                        itemEntity.setPickUpDelay(10);
                        world.addFreshEntity(itemEntity);
                    }
                }

                entity.remove(Entity.RemovalReason.DISCARDED);
                break;
            }
        }
    }
}