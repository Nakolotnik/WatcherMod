package org.nakolotnik.wt.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Axis;
import org.nakolotnik.wt.entity.DarkEyesEntity;

public class DarkEyesRenderer extends EntityRenderer<DarkEyesEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("wt", "textures/entity/eyes.png");

    public DarkEyesRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(DarkEyesEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0F, entity.getBbHeight() * 0.5F, 0.0F);

        float scale = 0.5F;
        poseStack.scale(scale, scale, scale);

        Vec3 lookVector = entity.getLookAngle();
        float yawAngle = (float) Math.toDegrees(Math.atan2(lookVector.x, lookVector.z));
        float pitchAngle = (float) Math.toDegrees(Math.atan2(lookVector.y, lookVector.horizontalDistance()));

        poseStack.mulPose(Axis.YP.rotationDegrees(yawAngle + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitchAngle));

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        renderQuad(poseStack, vertexConsumer, packedLight);

        poseStack.popPose();
    }

    private void renderQuad(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight) {
        PoseStack.Pose matrix = poseStack.last();
        float size = 0.3F;

        vertexConsumer.vertex(matrix.pose(), -size, -size, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(0, 10).uv2(packedLight).normal(matrix.normal(), 0, 0, -1).endVertex();
        vertexConsumer.vertex(matrix.pose(), -size, size, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(0, 10).uv2(packedLight).normal(matrix.normal(), 0, 0, -1).endVertex();
        vertexConsumer.vertex(matrix.pose(), size, size, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(0, 10).uv2(packedLight).normal(matrix.normal(), 0, 0, -1).endVertex();
        vertexConsumer.vertex(matrix.pose(), size, -size, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(0, 10).uv2(packedLight).normal(matrix.normal(), 0, 0, -1).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DarkEyesEntity entity) {
        return TEXTURE;
    }
}
