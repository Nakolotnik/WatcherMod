package org.nakolotnik.wt.procedures;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class TimeProcedure {
    private static final Map<UUID, AnimationState> runningAnimations = new HashMap<>();

    private static final int TOTAL_TICKS = 40;
    private static final int PARTICLE_COUNT = 50;
    private static final double CLOCK_RADIUS = 1.5;
    private static final double MINUTE_SPEED = 2 * Math.PI / TOTAL_TICKS;
    private static final double HOUR_SPEED = MINUTE_SPEED / 12;
    private static final double INITIAL_MINUTE_ANGLE = 2 * Math.PI * (10.0 / 60.0);
    private static final double INITIAL_HOUR_ANGLE = 2 * Math.PI * (10.0 / 12.0);

    public static void execute(Level world, Player player) {
        if (!(world instanceof ServerLevel serverLevel) || serverLevel.isClientSide) return;

        UUID uuid = player.getUUID();
        if (runningAnimations.containsKey(uuid)) return;

        Vec3 pos = player.position();
        Vec3 dir = player.getLookAngle().normalize();

        double centerX = pos.x + dir.x * 3;
        double centerY = pos.y + 2;
        double centerZ = pos.z + dir.z * 3;

        long currentTime = serverLevel.getDayTime() % 24000L;
        long targetTime = (currentTime < 12000L) ? 13000L : 24000 + 1000L;
        long timeDiff = targetTime - currentTime;
        long timeStep = timeDiff / TOTAL_TICKS;

        runningAnimations.put(uuid, new AnimationState(serverLevel, player, centerX, centerY, centerZ, dir, timeStep));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<UUID, AnimationState>> iter = runningAnimations.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, AnimationState> entry = iter.next();
            AnimationState anim = entry.getValue();

            if (anim.tick >= TOTAL_TICKS) {
                iter.remove();
                continue;
            }

            anim.animate();
            anim.tick++;
        }
    }

    private static class AnimationState {
        final ServerLevel level;
        final Player player;
        final double x, y, z;
        final Vec3 direction;
        final long timeStep;

        int tick = 0;
        double minuteAngle = INITIAL_MINUTE_ANGLE;
        double hourAngle = INITIAL_HOUR_ANGLE;

        AnimationState(ServerLevel level, Player player, double x, double y, double z, Vec3 direction, long timeStep) {
            this.level = level;
            this.player = player;
            this.x = x;
            this.y = y;
            this.z = z;
            this.direction = direction;
            this.timeStep = timeStep;
        }

        void animate() {
            level.setDayTime(level.getDayTime() + timeStep);

            drawClockFace();
            drawClockHands();

            minuteAngle += MINUTE_SPEED;
            hourAngle += HOUR_SPEED;

            if (tick % 5 == 0) {
                level.playSound(null, x, y, z, SoundEvents.NOTE_BLOCK_HAT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }

        void drawClockFace() {
            Vec3 right = new Vec3(-direction.z, 0, direction.x).normalize();
            Vec3 up = new Vec3(0, 1, 0);

            for (int i = 0; i < PARTICLE_COUNT; i++) {
                double angle = 2 * Math.PI * i / PARTICLE_COUNT;
                double offsetX = CLOCK_RADIUS * Math.cos(angle);
                double offsetZ = CLOCK_RADIUS * Math.sin(angle);

                double px = x + right.x * offsetX + up.x * offsetZ;
                double py = y + right.y * offsetX + up.y * offsetZ;
                double pz = z + right.z * offsetX + up.z * offsetZ;

                level.sendParticles(new DustParticleOptions(new Vec3(0.5F, 0.0F, 1.0F).toVector3f(), 1.2F),
                        px, py, pz, 1, 0, 0, 0, 0);
            }
        }

        void drawClockHands() {
            Vec3 perp = new Vec3(-direction.z, 0, direction.x).normalize();

            drawLine(
                    x, y, z,
                    x + perp.x * CLOCK_RADIUS * Math.sin(minuteAngle),
                    y + CLOCK_RADIUS * Math.cos(minuteAngle),
                    z + perp.z * CLOCK_RADIUS * Math.sin(minuteAngle),
                    new Vec3(0.0F, 1.0F, 1.0F)
            );

            drawLine(
                    x, y, z,
                    x + perp.x * CLOCK_RADIUS * 0.5 * Math.sin(hourAngle),
                    y + CLOCK_RADIUS * 0.5 * Math.cos(hourAngle),
                    z + perp.z * CLOCK_RADIUS * 0.5 * Math.sin(hourAngle),
                    new Vec3(0.0F, 0.5F, 1.0F)
            );
        }

        void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, Vec3 color) {
            final int segments = 12;
            for (int i = 0; i <= segments; i++) {
                double t = i / (double) segments;
                double px = x1 + (x2 - x1) * t;
                double py = y1 + (y2 - y1) * t;
                double pz = z1 + (z2 - z1) * t;

                level.sendParticles(
                        new DustParticleOptions(color.toVector3f(), 1.5F),
                        px, py, pz,
                        1, 0, 0, 0, 0
                );
            }
        }
    }
}