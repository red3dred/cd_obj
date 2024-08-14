package com.invasion.client.render;

import java.util.Map;

import com.invasion.client.render.animation.AnimationRegistry;
import com.invasion.client.render.animation.BonesMouth;
import com.invasion.client.render.animation.ModelAnimator;
import com.invasion.client.render.entity.model.ModelGiantBird;
import com.invasion.entity.EntityIMBird;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.util.math.MathHelper;

public class ModelVulture extends ModelGiantBird<EntityIMBird> {
    private final ModelAnimator<?> animationBeak;

    public ModelVulture(ModelPart root) {
        super(root);
        animationBeak = AnimationRegistry.instance().<BonesMouth>getAnimation("bird_beak").createAnimator(Map.of(
                BonesMouth.UPPER_MOUTH, head.getChild("upper_beak"),
                BonesMouth.LOWER_MOUTH, head.getChild("lower_beak")
        ));
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData body = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-10, -10, -10, 20, 30, 20), ModelTransform.of(0, -19, 0, 0.7F, 0, 0));
        createFoot(body.addChild("right_thigh", ModelPartBuilder.create().uv(84, 82).cuboid(-4.5F, -3.5F, -4.5F, 9, 15, 9), ModelTransform.of(-5, 20, -2, -0.39F, 0, 0.09F))
                .addChild("leg", ModelPartBuilder.create().uv(56, 50).cuboid(-2, -3, -2, 4, 16, 4), ModelTransform.of(0, 11, 0, -0.72F, 0, 0))
                .addChild("ankle", ModelPartBuilder.create().uv(16, 16).cuboid(0, 0, 0, 0, 0, 0), ModelTransform.of(0, 12, 0, 0.1F, 0.2F, 0)),
                -36, 1
        );
        createFoot(body.addChild("left_thigh", ModelPartBuilder.create().uv(84, 82).mirrored().cuboid(-4.5F, -3.5F, -4.5F, 9, 15, 9), ModelTransform.of(5, 20, -2, -0.39F, 0, -0.09F))
                .addChild("leg", ModelPartBuilder.create().uv(56, 50).mirrored().cuboid(-2, -3, -2, 4, 16, 4), ModelTransform.of(0, 11, 0, -0.72F, 0, 0))
                .addChild("ankle", ModelPartBuilder.create().uv(16, 16).cuboid(0, 0, 0, 0, 0, 0), ModelTransform.of(0, 12, 0, 0.1F, -0.2F, 0)),
                0, -1
        );

        ModelPartData neck3 = body
            .addChild("neck", ModelPartBuilder.create().uv(43, 95).cuboid(-7, -7, -6.5F, 14, 10, 13), ModelTransform.of(0, -10, 1, -0.18F, 0, 0))
            .addChild("neck", ModelPartBuilder.create().uv(50, 73).cuboid(-5, -4, -5, 10, 8, 10), ModelTransform.of(0, -8, 0, 0.52F, 0, 0))
            .addChild("neck", ModelPartBuilder.create().uv(80, 65).cuboid(-4, -5.5F, -5, 8, 5, 10), ModelTransform.of(0, -2, 0, 0.26F, 0, 0));

        ModelPartData head = neck3.addChild("head", ModelPartBuilder.create().uv(14, 108).cuboid(-4.5F, -5, -9.5F, 9, 8, 11), ModelTransform.of(0, -4, 0, -0.97F, 0, 0));
        head.addChild("upper_beak", ModelPartBuilder.create().uv(54, 118).cuboid(-2.5F, -1, -5, 5, 2, 8), ModelTransform.pivot(0, -0.8F, -10))
                .addChild("tip", ModelPartBuilder.create().uv(72, 118).cuboid(-1, -1, -1, 2, 2, 2), ModelTransform.pivot(0, 0, -6));
        head.addChild("lower_beak", ModelPartBuilder.create().uv(80, 118).cuboid(-2.5F, -1, -5, 5, 2, 8), ModelTransform.pivot(0, 1.5F, -10))
                .addChild("tip", ModelPartBuilder.create().uv(78, 121).cuboid(-1, -0.5F, -1, 2, 1, 2), ModelTransform.pivot(0, -0.5F, -6));

        body.addChild("tail", ModelPartBuilder.create().uv(80, 23).cuboid(-8.5F, -5, -1, 17, 40, 2), ModelTransform.of(0, 19, 8, 0.3F, 0, 0));
        body.addChild("right_wing", ModelPartBuilder.create().uv(0, 50).cuboid(-24.5F, -4.5F, -1.5F, 25, 29, 3), ModelTransform.pivot(-7, -8, 6))
            .addChild("elbow", ModelPartBuilder.create().uv(0, 82).cuboid(-20.5F, -5, -1, 23, 24, 2), ModelTransform.pivot(-23, 1, 0))
            .addChild("tip", ModelPartBuilder.create().uv(80, 0).cuboid(-20.5F, -5, -0.5F, 23, 22, 1), ModelTransform.pivot(-21, 0.2F, 0.3F));
        body.addChild("left_wing", ModelPartBuilder.create().uv(0, 50).mirrored().cuboid(-0.5F, -4.5F, -1.5F, 25, 29, 3), ModelTransform.pivot(7, -8, 6))
            .addChild("elbow", ModelPartBuilder.create().uv(0, 82).mirrored().cuboid(-2.5F, -5, -1, 23, 24, 2), ModelTransform.pivot(23, 1, 0))
            .addChild("tip", ModelPartBuilder.create().uv(80, 0).mirrored().cuboid(-2.5F, -5, -0.5F, 23, 22, 1), ModelTransform.pivot(21, 0.2F, 0.3F));

        return TexturedModelData.of(data, 128, 128);
    }

    private static void createFoot(ModelPartData ankle, float clawAngle, int mirror) {
        ankle
            .addChild("back_toe", ModelPartBuilder.create().uv(60, 0).cuboid(-1, -1, -1, 2, 8, 2), ModelTransform.of(0, 0, 2, 1.34F, 0, 0))
            .addChild("claw", ModelPartBuilder.create().uv(0, 11).cuboid(-0.5F, 0, -1, 1, 4, 2), ModelTransform.of(0, 6, 0, clawAngle, 0, 0));
        ankle
            .addChild("left_toe", ModelPartBuilder.create().uv(0, 0).cuboid(-1, 0.5F, -1, 2, 9, 2), ModelTransform.of(0.5F * mirror, 0, 1, -0.8F, -0.28F, -0.28F))
            .addChild("claw", ModelPartBuilder.create().uv(0, 11).cuboid(-0.5F, 0, -1, 1, 4, 2), ModelTransform.pivot(0, 9, 0));
        ankle
            .addChild("middle_toe", ModelPartBuilder.create().uv(8, 0).cuboid(-1, 0, -1, 2, 10, 2), ModelTransform.rotation(-0.8F, 0, 0))
            .addChild("claw", ModelPartBuilder.create().uv(0, 11).cuboid(-0.5F, 0, -1, 1, 4, 2), ModelTransform.pivot(0, 9, 0));
        ankle
            .addChild("right_toe", ModelPartBuilder.create().uv(0, 0).cuboid(-1, -0.5F, -1, 2, 9, 2), ModelTransform.of(-1 * mirror, 0, 1, -0.8F, 0.28F, 0.28F))
            .addChild("claw", ModelPartBuilder.create().uv(0, 11).cuboid(-0.5F, 0, -1, 1, 4, 2), ModelTransform.pivot(0, 8, 0));
    }

    @Override
    protected void animateRunning(EntityIMBird entity, float legProgress) {
        if (legProgress >= 0.109195F && legProgress < 0.5373563F) {
            legProgress += 0.03735632F;
            if (legProgress >= 0.5373563F) {
                legProgress -= 0.4281609F;
            }
            float t = MathHelper.cos(25.132742F * legProgress / 0.8908046F);
            body.pitch += -t * 0.04F;
            neck1.pitch += t * 0.08F;
            body.pivotY += -t * 1.9F;
        }
    }

    @Override
    protected void updateAnimations(EntityIMBird entity, float tickDelta) {
        super.updateAnimations(entity, tickDelta);
        animationBeak.updateAnimation(entity.getBeakAnimationState().getCurrentAnimationTimeInterp(tickDelta));
    }
}