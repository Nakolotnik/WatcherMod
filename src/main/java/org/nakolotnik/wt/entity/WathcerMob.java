package org.nakolotnik.wt.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.nakolotnik.wt.init.ModAnimations;
import org.nakolotnik.wt.init.ModEntity;
import org.nakolotnik.wt.procedures.WatcherLookProcedure;
import org.nakolotnik.wt.procedures.WatcherIdleProcedure;
import org.zeith.hammeranims.api.animsys.*;
import org.zeith.hammeranims.api.animsys.layer.AnimationLayer;
import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

public class WathcerMob extends Mob implements IAnimatedEntity {
    protected final AnimationSystem animations = AnimationSystem.create(this);

    public WathcerMob(EntityType<? extends Mob> type, Level world) {
        super(type, world);
        this.setCustomNameVisible(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0);
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public void tick() {
        animations.tick();
        super.tick();
        WatcherIdleProcedure.execute(this);

        if (!this.level().isClientSide()) {
            forceLookAtPlayer();

            Player player = level().getNearestPlayer(this, 10);
            if (player instanceof ServerPlayer serverPlayer) {
                WatcherLookProcedure.execute(this, serverPlayer);
            }
        }

        animations.startAnimationAt(CommonLayerNames.AMBIENT, ModAnimations.WATCHER_IDLE);
    }

    private void forceLookAtPlayer() {
        Player player = this.level().getNearestPlayer(this, 30);
        if (player != null) {
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            double dy = player.getEyeY() - this.getEyeY();

            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
            float pitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));

            this.setYRot(yaw);
            this.setXRot(pitch);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
            this.yHeadRotO = yaw;
        }
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        builder.autoSync().addLayers(
                AnimationLayer.builder(CommonLayerNames.AMBIENT).preventAutoSync(),
                AnimationLayer.builder(CommonLayerNames.ACTION)
        );
    }

    @Override
    public AnimationSystem getAnimationSystem() {
        return animations;
    }

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntity.WATCHER, WathcerMob.createAttributes().build());
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.animations.deserializeNBT(pCompound.getCompound("Animations"));
        super.readAdditionalSaveData(pCompound);
    }

    @Override
    public IGeometryContainer getObjectModel() {
        return null;
    }
}
