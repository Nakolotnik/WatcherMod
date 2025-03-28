package org.nakolotnik.wt.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import java.util.List;

public class DarkEyesEntity extends Entity {
    private static final int LIFESPAN_TICKS = 20 * 20;
    private int tickCount = 0;

    public DarkEyesEntity(EntityType<? extends DarkEyesEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        tickCount++;

        if (!this.level().isClientSide && tickCount >= LIFESPAN_TICKS) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide) {
            List<ServerPlayer> players = ((ServerLevel) this.level()).getPlayers(player -> this.distanceTo(player) < 10);
            if (!players.isEmpty()) {
                ServerPlayer target = players.get(0);
                lookAtPlayer(target);
            }
        }
    }

    private void lookAtPlayer(LivingEntity player) {
        Vec3 direction = player.position().subtract(this.position()).normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.asin(direction.y));

        this.setYRot(yaw);
        this.setXRot(pitch);
        this.setYHeadRot(yaw);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tickCount = tag.getInt("TickCount");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TickCount", tickCount);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
