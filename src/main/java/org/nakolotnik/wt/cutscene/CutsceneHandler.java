package org.nakolotnik.wt.cutscene;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "wt", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.CLIENT)
public class CutsceneHandler {
    private static long lastUpdateTime = 0;
    private static final float MILLIS_TO_SECONDS = 1.0f / 1000.0f;
    private static final float PROGRESS_INCREMENT = 0.002f;

    private static float normalizeAngle(float angle1, float angle2, float t) {
        float diff = angle2 - angle1;
        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;
        return angle1 + diff * t;
    }

    private static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        double x = 0.5 * ((2 * p1.x) + (-p0.x + p2.x) * t + (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * t2 + (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * t3);
        double y = 0.5 * ((2 * p1.y) + (-p0.y + p2.y) * t + (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * t2 + (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * t3);
        double z = 0.5 * ((2 * p1.z) + (-p0.z + p2.z) * t + (2 * p0.z - 5 * p1.z + 4 * p2.z - p3.z) * t2 + (-p0.z + 3 * p1.z - 3 * p2.z + p3.z) * t3);
        return new Vec3(x, y, z);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && CutsceneManager.isCutsceneActive() && event.player == Minecraft.getInstance().player) {
            if (CutsceneManager.getCurrentCutscene() == null || CutsceneManager.getProgress() >= 1.0f) {
                CutsceneManager.stopCutscene();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !CutsceneManager.isCutsceneActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        long currentTimeMillis = System.currentTimeMillis();
        float deltaTime = lastUpdateTime > 0 ? (currentTimeMillis - lastUpdateTime) * MILLIS_TO_SECONDS : 0.016f;
        lastUpdateTime = currentTimeMillis;

        Player player = mc.player;
        CutsceneManager.Cutscene currentCutscene = CutsceneManager.getCurrentCutscene();
        if (currentCutscene == null) {
            lastUpdateTime = 0;
            CutsceneManager.stopCutscene();
            return;
        }

        CutsceneManager.incrementProgress(deltaTime * PROGRESS_INCREMENT);
        float progress = CutsceneManager.getProgress();
        List<CutsceneManager.CutsceneFrame> frames = currentCutscene.frames;

        if (progress >= 1.0f) {
            CutsceneManager.CutsceneFrame lastFrame = frames.get(frames.size() - 1);
            Vec3 finalPosition = CutsceneManager.getCurrentPosition(lastFrame.position);
            player.setPos(finalPosition.x, finalPosition.y, finalPosition.z);
            player.setYRot(lastFrame.yaw);
            player.setXRot(lastFrame.pitch);
            triggerEvents(lastFrame.events);
            lastUpdateTime = 0;
            CutsceneManager.stopCutscene();
            return;
        }

        float cumulativeWeight = 0.0f;
        int currentFrameIdx = 0;
        for (int i = 0; i < frames.size() - 1; i++) {
            cumulativeWeight += frames.get(i).normalizedWeight;
            if (progress <= cumulativeWeight) {
                currentFrameIdx = i;
                break;
            }
        }

        Vec3 p0 = currentFrameIdx > 0 ? CutsceneManager.getCurrentPosition(frames.get(currentFrameIdx - 1).position) : CutsceneManager.getCurrentPosition(frames.get(0).position);
        Vec3 p1 = CutsceneManager.getCurrentPosition(frames.get(currentFrameIdx).position);
        Vec3 p2 = CutsceneManager.getCurrentPosition(frames.get(currentFrameIdx + 1).position);
        Vec3 p3 = currentFrameIdx < frames.size() - 2 ? CutsceneManager.getCurrentPosition(frames.get(currentFrameIdx + 2).position) : p2;

        float segmentStartProgress = currentFrameIdx > 0 ? cumulativeWeight - frames.get(currentFrameIdx).normalizedWeight : 0.0f;
        float segmentEndProgress = cumulativeWeight;
        float segmentProgress = (progress - segmentStartProgress) / (segmentEndProgress - segmentStartProgress);
        float t = Math.min(1.0f, Math.max(0.0f, segmentProgress));

        Vec3 interpolatedPos = catmullRom(p0, p1, p2, p3, t);

        float yaw1 = frames.get(currentFrameIdx).yaw;
        float yaw2 = frames.get(currentFrameIdx + 1).yaw;
        float pitch1 = frames.get(currentFrameIdx).pitch;
        float pitch2 = frames.get(currentFrameIdx + 1).pitch;
        float interpolatedYaw = normalizeAngle(yaw1, yaw2, t);
        float interpolatedPitch = normalizeAngle(pitch1, pitch2, t);

        player.setPos(interpolatedPos.x, interpolatedPos.y, interpolatedPos.z);
        player.setYRot(interpolatedYaw);
        player.setXRot(interpolatedPitch);

        triggerEvents(frames.get(currentFrameIdx).events);
    }

    private static void triggerEvents(List<CutsceneManager.CutsceneEvent> events) {
        if (events == null || events.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        for (CutsceneManager.CutsceneEvent event : events) {
            switch (event.type.toLowerCase()) {
                case "sound":
                    mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new ResourceLocation(event.value)), net.minecraft.sounds.SoundSource.MASTER, 1.0f, 1.0f, false);
                    break;
                case "command":
                    mc.player.connection.sendCommand(event.value);
                    break;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (CutsceneManager.isCutsceneActive() && event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
        }
    }
}