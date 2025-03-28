package org.nakolotnik.wt.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.Random;

public class TimeRiftEntity extends Entity {

    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(TimeRiftEntity.class, EntityDataSerializers.INT);
    private static final double PULL_RADIUS = 10.0; // Радиус притяжения
    private static final double PULL_STRENGTH = 1.5; // Базовая сила притяжения
    private static final double MIN_PULL_FORCE = 0.05; // Минимальная сила притяжения
    private static final double MAX_PULL_FORCE = 5; // Максимальная сила притяжения
    private static final double CLOSE_RANGE = 1.0; // Дистанция для эффекта в центре
    private static final double INTENSE_RANGE = 2.0; // Дистанция для усиленного притяжения
    private static final double INTENSE_MULTIPLIER = 2.0; // Множитель для усиленного притяжения
    private static final int LIFETIME_TICKS = 60*20; // 60 секунд (20 тиков в секунде)

    private int ticksLived = 0;

    public TimeRiftEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SIZE, 10);
    }

    @Override
    public void tick() {
        super.tick();

        ticksLived++;
        if (ticksLived >= LIFETIME_TICKS) {
            if (this.level().isClientSide) {
                for (int i = 0; i < 20; i++) {
                    this.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.PORTAL,
                            this.getX() + (this.random.nextDouble() - 0.5) * 5.0,
                            this.getY() + (this.random.nextDouble() - 0.5) * 2.5,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 5.0,
                            0.0, 0.0, 0.0
                    );
                }

            }
            this.discard();
            return;
        }

        if (!this.level().isClientSide) {
            AABB pullArea = new AABB(
                    this.getX() - PULL_RADIUS, this.getY() - PULL_RADIUS, this.getZ() - PULL_RADIUS,
                    this.getX() + PULL_RADIUS, this.getY() + PULL_RADIUS, this.getZ() + PULL_RADIUS
            );

            Vec3 riftCenter = this.position();

            List<LivingEntity> livingEntities = this.level().getEntitiesOfClass(LivingEntity.class, pullArea);
            for (LivingEntity entity : livingEntities) {
                applyGravity(entity, riftCenter);
            }

            List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, pullArea);
            for (ItemEntity item : items) {
                applyGravity(item, riftCenter);
            }
        }
    }

    private void applyGravity(Entity entity, Vec3 riftCenter) {
        Vec3 entityPos = entity.position();
        Vec3 direction = riftCenter.subtract(entityPos);
        double distance = direction.length();

        if (distance < 0.1) return;
        direction = direction.normalize();
        double pullForce = PULL_STRENGTH / (distance * distance);

        if (distance < INTENSE_RANGE) {
            double intensityFactor = 1.0 + INTENSE_MULTIPLIER * (1.0 - distance / INTENSE_RANGE);
            pullForce *= intensityFactor;
        }

        pullForce = Math.min(MAX_PULL_FORCE, Math.max(MIN_PULL_FORCE, pullForce));

        double mass = 1.0;
        if (entity instanceof LivingEntity living) {
            mass += living.getArmorValue() * 0.05;
            if (living instanceof Player player) {
                mass += player.getInventory().getContainerSize() * 0.01;
            }
        } else if (entity instanceof ItemEntity) {
            mass = 0.5;
        }
        pullForce /= mass;

        Vec3 pullVector = direction.scale(pullForce);
        entity.setDeltaMovement(entity.getDeltaMovement().add(pullVector));
        entity.hurtMarked = true;

        if (distance < CLOSE_RANGE) {
            if (entity instanceof Player player) {
                player.hurt(this.level().damageSources().magic(), 2.0f);
                teleportEntity(player);
            } else if (entity instanceof ItemEntity item) {
                item.discard();
            } else {
                entity.hurt(this.level().damageSources().magic(), 2.0f);
            }
        }

        if (this.level().isClientSide) {
            float particleChance = (float) (1.0 - distance / PULL_RADIUS) * 0.8f;
            if (this.random.nextFloat() < particleChance) {
                this.level().addParticle(
                        net.minecraft.core.particles.ParticleTypes.PORTAL,
                        entity.getX(), entity.getY() + 1.0, entity.getZ(),
                        (riftCenter.x - entity.getX()) * 0.1,
                        (riftCenter.y - entity.getY()) * 0.1,
                        (riftCenter.z - entity.getZ()) * 0.1
                );
                if (distance < INTENSE_RANGE) {
                    this.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.ENCHANT,
                            entity.getX(), entity.getY() + 1.0, entity.getZ(),
                            (riftCenter.x - entity.getX()) * 0.2,
                            (riftCenter.y - entity.getY()) * 0.2,
                            (riftCenter.z - entity.getZ()) * 0.2
                    );
                }
            }

            float soundChance = (float) (1.0 - distance / PULL_RADIUS) * 0.1f;
            if (this.random.nextFloat() < soundChance) {
                this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        net.minecraft.sounds.SoundEvents.AMBIENT_CAVE.get(), net.minecraft.sounds.SoundSource.AMBIENT,
                        0.5f, 0.5f + (float) (1.0 - distance / PULL_RADIUS));
            }
        }
    }

    private void teleportEntity(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ResourceKey<Level> destination = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("wt:timeless_wasteland"));

        if (serverPlayer.level().dimension() == destination) return;

        ServerLevel targetLevel = serverPlayer.server.getLevel(destination);
        if (targetLevel == null) return;

        BlockPos targetPos = findSafePositionNearCenter(targetLevel, serverPlayer.position());

        serverPlayer.teleportTo(targetLevel, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5,
                serverPlayer.getYRot(), serverPlayer.getXRot());

    }

    private BlockPos findSafePositionNearCenter(ServerLevel level, Vec3 basePos) {
        Random rand = new Random();
        int centerX = (int) basePos.x;
        int centerZ = (int) basePos.z;

        int radius = 12;
        int minY = Math.max(level.getMinBuildHeight(), 40);
        int maxY = Math.min(level.getMaxBuildHeight(), 150);

        for (int attempt = 0; attempt < 80; attempt++) {
            int dx = centerX + rand.nextInt(radius * 2 + 1) - radius;
            int dz = centerZ + rand.nextInt(radius * 2 + 1) - radius;
            int dy = maxY - attempt / 2;

            BlockPos pos = new BlockPos(dx, dy, dz);

            if (isSafePosition(level, pos)) {
                return pos;
            }
        }

        return new BlockPos(centerX, 80, centerZ);
    }

    private boolean isSafePosition(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.below()).isSolidRender(level, pos.below());
    }


    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(SIZE, tag.getInt("Size"));
        if (tag.contains("TicksLived")) {
            this.ticksLived = tag.getInt("TicksLived");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Size", this.entityData.get(SIZE));
        tag.putInt("TicksLived", this.ticksLived);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}