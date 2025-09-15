package com.invasion.client.render.entity.model;

import java.util.Map;

import com.invasion.client.render.animation.AnimationAction;
import com.invasion.client.render.animation.AnimationRegistry;
import com.invasion.client.render.animation.BirdLegBone;
import com.invasion.client.render.animation.WingBone;
import com.invasion.client.render.animation.Animator;
import com.invasion.entity.VultureEntity;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class ModelGiantBird<T extends VultureEntity> extends SinglePartEntityModel<T> {
    protected final Animator<WingBone> animationFlap;
    protected final Animator<BirdLegBone> animationRun;

    protected final ModelPart body;
    protected final ModelPart tail;

    protected final ModelPart rightThigh;
    protected final ModelPart leftThigh;

    protected final ModelPart neck1;
    protected final ModelPart neck2;
    protected final ModelPart neck3;
    protected final ModelPart head;

    public ModelGiantBird(ModelPart root) {
        body = root.getChild("body");
        tail = body.getChild("tail");
        rightThigh = body.getChild("right_thigh");
        leftThigh = body.getChild("left_thigh");
        neck1 = body.getChild("neck");
        neck2 = neck1.getChild("neck");
        neck3 = neck2.getChild("neck");
        head = neck3.getChild("head");


        animationRun = AnimationRegistry.instance().<BirdLegBone>get("bird_run").createAnimator(Map.of(
            BirdLegBone.LEFT_KNEE, leftThigh,
            BirdLegBone.RIGHT_KNEE, rightThigh,
            BirdLegBone.LEFT_ANKLE, leftThigh.getChild("leg"),
            BirdLegBone.RIGHT_ANKLE, rightThigh.getChild("leg"),
            BirdLegBone.LEFT_METATARSOPHALANGEAL_ARTICULATIONS, leftThigh.getChild("leg").getChild("ankle"),
            BirdLegBone.RIGHT_METATARSOPHALANGEAL_ARTICULATIONS, rightThigh.getChild("leg").getChild("ankle"),
            BirdLegBone.LEFT_BACK_CLAW, leftThigh.getChild("leg").getChild("ankle").getChild("back_toe"),
            BirdLegBone.RIGHT_BACK_CLAW, rightThigh.getChild("leg").getChild("ankle").getChild("back_toe")
        ));
        animationFlap = AnimationRegistry.instance().<WingBone>get("wing_flap_2_piece").createAnimator(Map.of(
                WingBone.LEFT_SHOULDER, body.getChild("left_wing"),
                WingBone.RIGHT_SHOULDER, body.getChild("right_wing"),
                WingBone.LEFT_ELBOW, body.getChild("left_wing").getChild("elbow"),
                WingBone.RIGHT_ELBOW, body.getChild("right_wing").getChild("elbow")
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
        createFoot(body.addChild("left_thigh", ModelPartBuilder.create().uv(84, 82).mirrored().cuboid(-4.5F, -3.5F, -4.5F, 9, 15, 9), ModelTransform.of(-5, 20, -2, -0.39F, 0, -0.09F))
                .addChild("leg", ModelPartBuilder.create().uv(56, 50).mirrored().cuboid(-2, -3, -2, 4, 16, 4), ModelTransform.of(0, 11, 0, -0.72F, 0, 0))
                .addChild("ankle", ModelPartBuilder.create().uv(16, 16).cuboid(0, 0, 0, 0, 0, 0), ModelTransform.of(0, 12, 0, 0.1F, -0.2F, 0)),
                0, -1
        );

        ModelPartData neck3 = body
                .addChild("neck", ModelPartBuilder.create().uv(43, 95).cuboid(-7, -7, -6.5F, 14, 10, 13), ModelTransform.of(0, -10, 1, -0.18F, 0, 0))
                .addChild("neck", ModelPartBuilder.create().uv(50, 73).cuboid(-5, -4, -5, 10, 8, 10), ModelTransform.of(0, -8, 0, 0.52F, 0, 0))
                .addChild("neck", ModelPartBuilder.create().uv(80, 65).cuboid(-4, -5.5F, -5, 8, 5, 10), ModelTransform.of(0, -2, 0, 0.26F, 0, 0));
        neck3.addChild("back_feathers", ModelPartBuilder.create().uv(-7, 108).cuboid(-4, 0, -1.5F, 8, 0, 7), ModelTransform.of(0, -3, 5, -1.11F, 0, 0));
        neck3.addChild("left_feathers", ModelPartBuilder.create().uv(-6, 115).cuboid(-3, 0, -1, 6, 0, 6), ModelTransform.of(4, -3, 2, -0.85F, -1.87F, 0.39F));
        neck3.addChild("right_feathers", ModelPartBuilder.create().uv(-6, 115).cuboid(-3, 0, -1, 6, 0, 6), ModelTransform.of(-4, -3, 2, -0.85F, 1.87F, -0.39F));

        ModelPartData head = neck3.addChild("head", ModelPartBuilder.create().uv(14, 108).cuboid(-4.5F, -5, -9.5F, 9, 8, 11), ModelTransform.of(0, -4, 0, -0.97F, 0, 0));
        head.addChild("upper_beak", ModelPartBuilder.create().uv(54, 118).cuboid(-2.5F, -1, -3, 5, 2, 6), ModelTransform.pivot(0, -0.8F, -10))
                .addChild("tip", ModelPartBuilder.create().uv(70, 118).cuboid(-1, -1, -1, 2, 2, 2), ModelTransform.pivot(0, 0, -4));
        head.addChild("lower_beak", ModelPartBuilder.create().uv(78, 118).cuboid(-2.5F, -1, -3, 5, 2, 6), ModelTransform.pivot(0, 1.5F, -10))
                .addChild("tip", ModelPartBuilder.create().uv(76, 121).cuboid(-1, -0.5F, -1, 2, 1, 2), ModelTransform.pivot(0, -0.5F, -4));
        head.addChild("feathers", ModelPartBuilder.create().uv(-5, 121).cuboid(-3.5F, 0, -0.5F, 7, 0, 5), ModelTransform.of(0, -5, 0, 0.38F, 0, 0));

        body.addChild("tail", ModelPartBuilder.create().uv(80, 23).cuboid(-8.5F, -5, -1, 17, 40, 2), ModelTransform.of(0, 19, 8, 0.3F, 0, 0));
        body.addChild("right_wing", ModelPartBuilder.create().uv(0, 50).cuboid(-24.5F, -4.5F, -1.5F, 25, 29, 3), ModelTransform.pivot(7, -8, 6))
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
            .addChild("left_toe", ModelPartBuilder.create().uv(0, 0).cuboid(-1, 0.5F, -1, 2, 9, 2), ModelTransform.of(-0.5F, 0, 1, -0.8F, mirror * 0.28F, mirror * 0.28F))
            .addChild("claw", ModelPartBuilder.create().uv(0, 11).cuboid(-0.5F, 0, -1, 1, 4, 2), ModelTransform.pivot(0, 9, 0));
        ankle
            .addChild("middle_toe", ModelPartBuilder.create().uv(8, 0).cuboid(-1, 0, -1, 2, 10, 2), ModelTransform.rotation(-0.8F, 0, 0))
            .addChild("claw", ModelPartBuilder.create().uv(0, 11).cuboid(-0.5F, 0, -1, 1, 4, 2), ModelTransform.pivot(0, 9, 0));
        ankle
            .addChild("right_toe", ModelPartBuilder.create().uv(0, 0).cuboid(-1, -0.5F, -1, 2, 9, 2), ModelTransform.of(1, 0, 1, -0.8F, mirror * -0.28F, mirror * -0.28F))
            .addChild("claw", ModelPartBuilder.create().uv(0, 11).cuboid(-0.5F, 0, -1, 1, 4, 2), ModelTransform.pivot(0, 8, 0));
    }

    @Override
    public ModelPart getPart() {
        return body;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        body.pitch += 0.8707963705062867F - headPitch * MathHelper.RADIANS_PER_DEGREE;
    }

    @Override
    public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
        float roll = entity.getRoll(tickDelta);
        float headYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevHeadYaw, entity.getHeadYaw());
        float headPitch = entity.getPitch(tickDelta);
        resetSkeleton();

        updateAnimations(entity, tickDelta);

        if (entity.getWingAnimationState().getCurrentAction() == AnimationAction.WINGFLAP) {
            float flapCycle = entity.getWingAnimationState().getCurrentAnimationTimeInterp(tickDelta) / 0.2714932F;

            body.pivotY += MathHelper.cos(flapCycle * MathHelper.TAU) * 1.4F;
            rightThigh.pitch += MathHelper.cos(flapCycle * MathHelper.TAU) * 0.08726646324990228F;
            leftThigh.pitch += MathHelper.cos(flapCycle * MathHelper.TAU) * 0.08726646324990228F;
            tail.pitch += MathHelper.cos(flapCycle * MathHelper.TAU) * 0.03490658588512815F;
        }

        body.roll = (-roll / 180 * 3.141593F);

        headPitch = MathHelper.clamp(MathHelper.wrapDegrees(headPitch), -56.650002F, 37.16F);
        float pitchFactor = (headPitch + 56.650002F) / 93.800003F;
        head.pitch += -0.96F + pitchFactor * -0.1400001F;
        neck3.pitch += 0.378F + pitchFactor * -0.528F;
        neck2.pitch += 0.4F + pitchFactor * -0.4F;
        neck1.pitch += 0.513F + pitchFactor * -0.613F;

        headYaw = MathHelper.clamp(MathHelper.wrapDegrees(headYaw), -30.5F, 30.5F);
        float yawFactor = (headYaw + 30.5F) / 61;
        head.roll += 0.8F + yawFactor * 2 * -0.8F;
        neck3.roll += 0.38F + yawFactor * 2 * -0.38F;
        neck2.roll += 0.14F + yawFactor * 2 * -0.14F;
        head.yaw += -0.7F + yawFactor * 2 * 0.7F;
        neck3.yaw += -0.12F + yawFactor * 2 * 0.12F;
    }

    public void resetSkeleton() {
        getPart().traverse().forEach(ModelPart::resetTransform);
    }

    protected void animateRunning(T entity, float legProgress) {
        if (legProgress >= 0.109195F && legProgress < 0.5373563F) {
            float t = MathHelper.cos(25.132742F * legProgress / 0.7967914F);
            body.pitch += -t * 0.1F;
            neck1.yaw += t * 0.08F;
            body.roll += -t;
        }
    }

    protected void updateAnimations(T entity, float tickDelta) {
        float flapProgress = entity.getWingAnimationState().getCurrentAnimationTimeInterp(tickDelta);
        float legProgress = entity.getLegAnimationState().getCurrentAnimationTimeInterp(tickDelta);
        animationFlap.updateAnimation(flapProgress);
        animationRun.updateAnimation(legProgress);
        if (entity.getLegAnimationState().getCurrentAction() == AnimationAction.RUN) {
            animateRunning(entity, legProgress);
        }
    }
}