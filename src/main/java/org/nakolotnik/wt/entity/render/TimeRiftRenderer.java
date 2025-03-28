package org.nakolotnik.wt.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.nakolotnik.wt.entity.TimeRiftEntity;
import org.nakolotnik.wt.utils.ShaderHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TimeRiftRenderer extends EntityRenderer<TimeRiftEntity> {

    private static final int POINT_COUNT = 80;           // Больше точек для плавности
    private static final float BASE_RADIUS = 0.5f;       // Базовый радиус (ширина)
    private static final float HEIGHT = 2.0f;            // Высота разлома
    private static final float OUTLINE_OFFSET = 0.1f;    // Более заметный контур
    private static final float TAPER_FACTOR = 0.2f;      // Лёгкое сужение к краям

    private Map<UUID, Vec3[]> basePointsCache = new HashMap<>();

    public TimeRiftRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f; // Без тени
    }

    @Override
    public void render(TimeRiftEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float time = (entity.tickCount + partialTicks) * 0.05f;
        float pulse = 1.0f + Mth.sin(time * 1.2f) * 0.1f;

        VertexConsumer mainConsumer = buffer.getBuffer(ShaderHelper.RIFT);
        VertexConsumer outlineConsumer = buffer.getBuffer(ShaderHelper.RIFT);

        poseStack.pushPose();

        AABB boundingBox = entity.getBoundingBox();
        double centerX = (boundingBox.minX + boundingBox.maxX) / 2.0;
        double centerY = (boundingBox.minY + boundingBox.maxY) / 2.0;
        double centerZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0;
        poseStack.translate(centerX - entity.getX(), centerY - entity.getY(), centerZ - entity.getZ());

        float scaleX = (float) (boundingBox.maxX - boundingBox.minX) / 2.0f * 0.8f;
        float scaleY = (float) (boundingBox.maxY - boundingBox.minY) / 2.0f * 1.5f;
        float scaleZ = (float) (boundingBox.maxZ - boundingBox.minZ) / 2.0f * 0.2f;
        poseStack.scale(scaleX, scaleY, scaleZ);

        Matrix4f matrix = poseStack.last().pose();

        drawRiftPolygon(entity.getUUID(), matrix, mainConsumer, time, pulse, false, entity);
        drawRiftPolygon(entity.getUUID(), matrix, outlineConsumer, time, pulse, true, entity);

        poseStack.popPose();
    }

    private void drawRiftPolygon(UUID seed, Matrix4f matrix, VertexConsumer consumer, float time, float scale, boolean isOutline, TimeRiftEntity entity) {
        Random rand = new Random(seed.getMostSignificantBits() ^ seed.getLeastSignificantBits());

        float shapeNoise = rand.nextFloat() * 0.3f + 0.8f;
        float shapeTwist = rand.nextFloat() * 1.5f - 0.75f;
        float waveFreq = rand.nextFloat() * 3f + 3f;
        float waveAmp = rand.nextFloat() * 0.2f + 0.15f;
        float pulseFreq = rand.nextFloat() * 3.0f + 2.0f;
        float pulseAmp = rand.nextFloat() * 0.15f + 0.1f;

        Vec3 center = new Vec3(0, 0, 0);
        Vec3[] points;

        // Обновление кэша только если его нет или размер не совпадает
        if (!basePointsCache.containsKey(seed) || basePointsCache.get(seed).length != POINT_COUNT) {
            points = new Vec3[POINT_COUNT];
            float verticalStretch = 2.5f;

            for (int i = 0; i < POINT_COUNT; i++) {
                float t = i / (float) POINT_COUNT;
                double angle = t * Mth.TWO_PI;
                double y = t * HEIGHT - HEIGHT / 2f;

                float heightFactor = 1.0f - (float) Math.pow(Math.abs(y) / (HEIGHT / 2f), 1.5f) * (1.0f - TAPER_FACTOR);
                double base = BASE_RADIUS * heightFactor * (0.95f + rand.nextFloat() * 0.1f);

                double x = Math.cos(angle + shapeTwist * t) * base;
                double z = Math.sin(angle + shapeTwist * t) * base * 0.2f;

                points[i] = new Vec3(x, y * verticalStretch, z);
            }
            basePointsCache.put(seed, smoothPoints(points));
        }

        points = new Vec3[POINT_COUNT];
        Vec3[] basePoints = basePointsCache.get(seed);

        float dynamicStretch = 1.0f + Mth.sin(time * 0.5f) * 0.15f;

        for (int i = 0; i < POINT_COUNT; i++) {
            Vec3 base = basePoints[i];
            float t = i / (float) POINT_COUNT;
            double angle = t * Mth.TWO_PI;

            double waveX = Mth.sin((float) (angle * waveFreq + time)) * waveAmp * shapeNoise;
            double waveZ = Mth.cos((float) (angle * waveFreq + time)) * waveAmp * shapeNoise * 0.2f;
            double pulse = Mth.sin((float) (base.y * pulseFreq + time)) * pulseAmp;

            double offset = isOutline ? OUTLINE_OFFSET * (1.0f - (float) Math.pow(Math.abs(base.y) / (HEIGHT / 2f), 1.5f) * (1.0f - TAPER_FACTOR)) : 0.0;

            double x = (base.x + waveX + offset + pulse) * scale * dynamicStretch;
            double y = (base.y + pulse) * scale;
            double z = (base.z + waveZ + offset) * scale * dynamicStretch;

            points[i] = new Vec3(x, y, z);
        }

        float r = isOutline ? 0.8f : 0.5f;
        float g = isOutline ? 0.4f : 0.2f;
        float b = isOutline ? 1.0f : 0.8f;
        float a = isOutline ? 0.6f : 0.9f;

        float scroll = (time * 0.2f) % 1.0f;

        // Исправленный цикл рендеринга
        for (int i = 0; i < POINT_COUNT; i++) {
            Vec3 current = points[i];
            Vec3 next = points[(i + 1) % POINT_COUNT]; // Безопасный доступ благодаря %

            float u1 = 0.5f + (float) current.x * 0.5f + scroll;
            float v1 = 0.5f + (float) current.y * 0.5f;
            float u2 = 0.5f + (float) next.x * 0.5f + scroll;
            float v2 = 0.5f + (float) next.y * 0.5f;

            consumer.vertex(matrix, (float) center.x, (float) center.y, (float) center.z)
                    .color(r, g, b, a)
                    .uv(0.5f + scroll, 0.5f)
                    .endVertex();

            consumer.vertex(matrix, (float) current.x, (float) current.y, (float) current.z)
                    .color(r, g, b, a)
                    .uv(u1, v1)
                    .endVertex();

            consumer.vertex(matrix, (float) next.x, (float) next.y, (float) next.z)
                    .color(r, g, b, a)
                    .uv(u2, v2)
                    .endVertex();
        }
    }

    private Vec3[] smoothPoints(Vec3[] points) {
        Vec3[] current = points;
        for (int pass = 0; pass < 3; pass++) {
            Vec3[] next = new Vec3[POINT_COUNT];
            for (int i = 0; i < POINT_COUNT; i++) {
                Vec3 prev = current[(i - 1 + POINT_COUNT) % POINT_COUNT];
                Vec3 curr = current[i];
                Vec3 nextPt = current[(i + 1) % POINT_COUNT];

                double x = (prev.x + curr.x + nextPt.x) / 3.0;
                double y = (prev.y + curr.y + nextPt.y) / 3.0;
                double z = (prev.z + curr.z + nextPt.z) / 3.0;

                next[i] = new Vec3(x, y, z);
            }
            current = next;
        }
        return current;
    }

    @Override
    public ResourceLocation getTextureLocation(TimeRiftEntity entity) {
        return ShaderHelper.RIFT_TEXTURE;
    }
}