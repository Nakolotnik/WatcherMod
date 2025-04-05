package org.nakolotnik.wt.procedures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class AnimatedEyeProcedure {
    private static final Map<UUID, EyeAnimationData> ACTIVE_ANIMATIONS = new HashMap<>();

    public static void execute(Entity entity, Level world, float eyeWidth, float eyeHeight) {
        if (entity == null || world == null || world.isClientSide())
            return;

        startEyeAnimation(entity, eyeWidth, eyeHeight);
    }

    private static void startEyeAnimation(Entity entity, float eyeWidth, float eyeHeight) {
        if (!(entity instanceof Player))
            return;
        UUID playerUUID = entity.getUUID();
        EyeAnimationData animationData = new EyeAnimationData(eyeWidth, eyeHeight);
        ACTIVE_ANIMATIONS.put(playerUUID, animationData);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Player player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null)
            return;

        UUID playerUUID = player.getUUID();
        EyeAnimationData animData = ACTIVE_ANIMATIONS.get(playerUUID);

        if (animData != null) {
            performEyeAnimation(player, Minecraft.getInstance().level, animData);
            if (animData.animationTick >= EyeAnimationData.TOTAL_ANIMATION_DURATION) {
                ACTIVE_ANIMATIONS.remove(playerUUID);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void performEyeAnimation(Player player, Level world, EyeAnimationData animData) {
        animData.animationTick++;

        AnimationPhase prevPhase = animData.getCurrentPhase();
        AnimationPhase currentPhase = animData.getCurrentPhase();

        if (prevPhase != currentPhase && currentPhase == AnimationPhase.CLOSING) {
            world.playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP,
                    SoundSource.PLAYERS,
                    0.5f,
                    1.5f,
                    false
            );
        }

        if (prevPhase != currentPhase && currentPhase == AnimationPhase.CLOSED) {
            world.playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.PLAYERS,
                    0.3f,
                    2.0f,
                    false
            );
        }

        switch (currentPhase) {
            case LOOK_CENTER:
                drawEye(player, world, 0.0f, animData);
                break;
            case LOOK_RIGHT:
                float rightProgress = (float)(animData.animationTick - animData.getPhaseStartTick(AnimationPhase.LOOK_RIGHT)) /
                        (float)EyeAnimationData.LOOK_RIGHT_DURATION;
                drawEye(player, world, rightProgress * 0.5f, animData);
                break;
            case LOOK_LEFT:
                float leftProgress = (float)(animData.animationTick - animData.getPhaseStartTick(AnimationPhase.LOOK_LEFT)) /
                        (float)EyeAnimationData.LOOK_LEFT_DURATION;
                drawEye(player, world, 0.5f - leftProgress * 1.0f, animData);
                break;
            case CLOSING:
                float closingProgress = (float)(animData.animationTick - animData.getPhaseStartTick(AnimationPhase.CLOSING)) /
                        (float)EyeAnimationData.CLOSING_DURATION;
                drawClosingEye(player, world, closingProgress, animData);
                break;
            case CLOSED:
                drawClosedEye(player, world, animData);
                break;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawEye(Player player, Level world, float pupilOffset, EyeAnimationData animData) {
        Vec3 eyePos = player.getEyePosition().add(player.getLookAngle().multiply(3, 3, 3));

        float eyeWidth = animData.eyeWidth;
        float eyeHeight = animData.eyeHeight;

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = player.getLookAngle().cross(up).normalize();
        up = right.cross(player.getLookAngle()).normalize();

        int numPoints = 20;
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = Math.cos(angle) * eyeWidth / 2;
            double y = Math.sin(angle) * eyeHeight / 2;

            Vec3 particlePos = eyePos.add(
                    right.multiply(x, x, x)).add(
                    up.multiply(y, y, y)
            );

            world.addParticle(
                    ParticleTypes.END_ROD,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0
            );
        }

        float pupilSize = 0.4f * Math.min(eyeWidth, eyeHeight) / 2; // Размер зрачка пропорционален размеру глаза
        Vec3 pupilCenter = eyePos.add(right.multiply(pupilOffset * (eyeWidth / 2 - pupilSize),
                pupilOffset * (eyeWidth / 2 - pupilSize),
                pupilOffset * (eyeWidth / 2 - pupilSize)));

        int pupilPoints = 10;
        for (int i = 0; i < pupilPoints; i++) {
            double angle = 2 * Math.PI * i / pupilPoints;
            double x = Math.cos(angle) * pupilSize;
            double y = Math.sin(angle) * pupilSize;

            Vec3 particlePos = pupilCenter.add(
                    right.multiply(x, x, x)).add(
                    up.multiply(y, y, y)
            );

            world.addParticle(
                    ParticleTypes.SMOKE,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawClosingEye(Player player, Level world, float closingProgress, EyeAnimationData animData) {
        Vec3 eyePos = player.getEyePosition().add(player.getLookAngle().multiply(3, 3, 3));

        float eyeWidth = animData.eyeWidth;
        float eyeHeight = animData.eyeHeight * (1.0f - closingProgress);

        if (eyeHeight < 0.05f) eyeHeight = 0.05f;

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = player.getLookAngle().cross(up).normalize();
        up = right.cross(player.getLookAngle()).normalize();

        int numPoints = 20;
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = Math.cos(angle) * eyeWidth / 2;
            double y = Math.sin(angle) * eyeHeight / 2;

            Vec3 particlePos = eyePos.add(
                    right.multiply(x, x, x)).add(
                    up.multiply(y, y, y)
            );

            world.addParticle(
                    ParticleTypes.END_ROD,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0
            );
        }

        if (closingProgress < 0.8f) {
            float pupilSize = 0.4f * Math.min(eyeWidth, eyeHeight) / 2 * (1.0f - closingProgress);
            Vec3 pupilCenter = eyePos;

            int pupilPoints = 10;
            for (int i = 0; i < pupilPoints; i++) {
                double angle = 2 * Math.PI * i / pupilPoints;
                double x = Math.cos(angle) * pupilSize;
                double y = Math.sin(angle) * pupilSize;

                Vec3 particlePos = pupilCenter.add(
                        right.multiply(x, x, x)).add(
                        up.multiply(y, y, y)
                );

                world.addParticle(
                        ParticleTypes.SMOKE,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0, 0
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawClosedEye(Player player, Level world, EyeAnimationData animData) {
        Vec3 eyePos = player.getEyePosition().add(player.getLookAngle().multiply(3, 3, 3));

        float eyeWidth = animData.eyeWidth;

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = player.getLookAngle().cross(up).normalize();

        int numPoints = 15;
        for (int i = 0; i < numPoints; i++) {
            float t = (float)i / (numPoints - 1) * 2 - 1;

            Vec3 particlePos = eyePos.add(
                    right.multiply(t * eyeWidth / 2, t * eyeWidth / 2, t * eyeWidth / 2)
            );

            world.addParticle(
                    ParticleTypes.END_ROD,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0
            );
        }
    }

    static class EyeAnimationData {
        static final int LOOK_CENTER_DURATION = 20;
        static final int LOOK_RIGHT_DURATION = 20;
        static final int LOOK_LEFT_DURATION = 20;
        static final int CLOSING_DURATION = 15;
        static final int CLOSED_DURATION = 10;
        static final int TOTAL_ANIMATION_DURATION =
                LOOK_CENTER_DURATION + LOOK_RIGHT_DURATION + LOOK_LEFT_DURATION + CLOSING_DURATION + CLOSED_DURATION;

        int animationTick = 0;
        float eyeWidth;
        float eyeHeight;


        public EyeAnimationData(float eyeWidth, float eyeHeight) {
            this.eyeWidth = eyeWidth;
            this.eyeHeight = eyeHeight;
        }
        public EyeAnimationData() {
            this(2.0f, 1.0f);
        }

        AnimationPhase getCurrentPhase() {
            if (animationTick < LOOK_CENTER_DURATION) {
                return AnimationPhase.LOOK_CENTER;
            } else if (animationTick < LOOK_CENTER_DURATION + LOOK_RIGHT_DURATION) {
                return AnimationPhase.LOOK_RIGHT;
            } else if (animationTick < LOOK_CENTER_DURATION + LOOK_RIGHT_DURATION + LOOK_LEFT_DURATION) {
                return AnimationPhase.LOOK_LEFT;
            } else if (animationTick < LOOK_CENTER_DURATION + LOOK_RIGHT_DURATION + LOOK_LEFT_DURATION + CLOSING_DURATION) {
                return AnimationPhase.CLOSING;
            } else {
                return AnimationPhase.CLOSED;
            }
        }

        int getPhaseStartTick(AnimationPhase phase) {
            switch (phase) {
                case LOOK_CENTER: return 0;
                case LOOK_RIGHT: return LOOK_CENTER_DURATION;
                case LOOK_LEFT: return LOOK_CENTER_DURATION + LOOK_RIGHT_DURATION;
                case CLOSING: return LOOK_CENTER_DURATION + LOOK_RIGHT_DURATION + LOOK_LEFT_DURATION;
                case CLOSED: return LOOK_CENTER_DURATION + LOOK_RIGHT_DURATION + LOOK_LEFT_DURATION + CLOSING_DURATION;
                default: return 0;
            }
        }
    }

    enum AnimationPhase {
        LOOK_CENTER,
        LOOK_RIGHT,
        LOOK_LEFT,
        CLOSING,
        CLOSED
    }
}