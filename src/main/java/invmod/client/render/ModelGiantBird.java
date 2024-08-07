package invmod.client.render;

import invmod.client.render.animation.AnimationAction;
import invmod.client.render.animation.AnimationState;
import invmod.client.render.animation.BonesBirdLegs;
import invmod.client.render.animation.BonesWings;
import invmod.client.render.animation.ModelAnimator;
import invmod.common.entity.EntityIMGiantBird;
import invmod.common.util.MathUtil;
import java.util.Map;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class ModelGiantBird extends SinglePartEntityModel<EntityIMGiantBird> {
    private final ModelAnimator<BonesWings> animationFlap;
    private final ModelAnimator<BonesBirdLegs> animationRun;

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;

    private final ModelPart rightThigh;
    private final ModelPart leftThigh;

    private final ModelPart tail;

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData body = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-10, -10, -10, 20, 30, 20), ModelTransform.of(0, -19, 0, 0.7F, 0, 0));
        createFoot(body.addChild("right_thigh", ModelPartBuilder.create().uv(84, 82).cuboid(-4.5F, -3.5F, -4.5F, 9, 15, 9), ModelTransform.of(-5, 20, -2, -0.39F, 0, 0.09F))
                .addChild("leg", ModelPartBuilder.create().uv(56, 50).cuboid(-2, -3, -2, 4, 16, 4), ModelTransform.of(0, 11, 0, -0.72F, 0, 0))
                .addChild("ankle", ModelPartBuilder.create().uv(16, 16).cuboid(0, 0, 0, 0, 0, 0), ModelTransform.of(0, 12, 0, 0.1F, 0.2F, 0)),
                -36, 1
        );
        createFoot(body.addChild("left_thigh", ModelPartBuilder.create().uv(84, 82).cuboid(-4.5F, -3.5F, -4.5F, 9, 15, 9).mirrored(), ModelTransform.of(-5, 20, -2, -0.39F, 0, -0.09F))
                .addChild("leg", ModelPartBuilder.create().uv(56, 50).cuboid(-2, -3, -2, 4, 16, 4).mirrored(), ModelTransform.of(0, 11, 0, -0.72F, 0, 0))
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
            .addChild("wing", ModelPartBuilder.create().uv(0, 82).cuboid(-20.5F, -5, -1, 23, 24, 2), ModelTransform.pivot(-23, 1, 0))
            .addChild("wing", ModelPartBuilder.create().uv(80, 0).cuboid(-20.5F, -5, -0.5F, 23, 22, 1), ModelTransform.pivot(-21, 0.2F, 0.3F));
        body.addChild("left_wing", ModelPartBuilder.create().uv(0, 50).cuboid(-0.5F, -4.5F, -1.5F, 25, 29, 3).mirrored(), ModelTransform.pivot(7, -8, 6))
            .addChild("wing", ModelPartBuilder.create().uv(0, 82).cuboid(-2.5F, -5, -1, 23, 24, 2).mirrored(), ModelTransform.pivot(23, 1, 0))
            .addChild("wing", ModelPartBuilder.create().uv(80, 0).cuboid(-2.5F, -5, -0.5F, 23, 22, 1).mirrored(), ModelTransform.pivot(21, 0.2F, 0.3F));

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

    public ModelGiantBird(ModelPart root) {
        this.body = root.getChild("body");
        this.rightThigh = body.getChild("right_thigh");
        this.leftThigh = body.getChild("left_thigh");
        this.neck1 = body.getChild("neck");
        this.neck2 = neck1.getChild("neck");
        this.neck3 = neck2.getChild("neck");
        this.head = neck3.getChild("head");
        this.tail = body.getChild("tail");

        animationRun = AnimationRegistry.instance().<BonesBirdLegs>getAnimation("bird_run").createAnimator(Map.of(
            BonesBirdLegs.LEFT_KNEE, leftThigh,
            BonesBirdLegs.RIGHT_KNEE, rightThigh,
            BonesBirdLegs.LEFT_ANKLE, leftThigh.getChild("leg"),
            BonesBirdLegs.RIGHT_ANKLE, rightThigh.getChild("leg"),
            BonesBirdLegs.LEFT_METATARSOPHALANGEAL_ARTICULATIONS, leftThigh.getChild("leg").getChild("ankle"),
            BonesBirdLegs.RIGHT_METATARSOPHALANGEAL_ARTICULATIONS, rightThigh.getChild("leg").getChild("ankle"),
            BonesBirdLegs.LEFT_BACK_CLAW, leftThigh.getChild("leg").getChild("ankle").getChild("back_toe"),
            BonesBirdLegs.RIGHT_BACK_CLAW, rightThigh.getChild("leg").getChild("ankle").getChild("back_toe")
        ));
        animationFlap = AnimationRegistry.instance().<BonesWings>getAnimation("wing_flap_2_piece").createAnimator(Map.of(
                BonesWings.LEFT_SHOULDER, body.getChild("left_wing"),
                BonesWings.RIGHT_SHOULDER, body.getChild("right_wing"),
                BonesWings.LEFT_ELBOW, body.getChild("left_wing").getChild("elbow"),
                BonesWings.RIGHT_ELBOW, body.getChild("right_wing").getChild("elbow")
        ));
    }

    @Override
    public ModelPart getPart() {
        return body;
    }

    @Override
    public void setAngles(EntityIMGiantBird entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        body.pitch = ((float) (body.pitch + (0.8707963705062867D - headPitch / 180 * 3.141593F)));
    }

    public void setFlyingAnimations(AnimationState<AnimationAction> wingState, AnimationState<AnimationAction> legState, float roll, float headYaw, float headPitch, float parTick) {
        float flapProgress = wingState.getCurrentAnimationTimeInterp(parTick);
        float legProgress = legState.getCurrentAnimationTimeInterp(parTick);
        this.animationFlap.updateAnimation(flapProgress);
        this.animationRun.updateAnimation(legProgress);
        if (legState.getCurrentAction() == AnimationAction.RUN) {
            if ((legProgress >= 0.109195F) && (legProgress < 0.5373563F)) {
                legProgress = (float) (legProgress + 0.0D);

                float t = 25.132742F * legProgress / 0.7967914F;
                this.body.pitch += (float) (-Math.cos(t) * 0.1D);
                this.neck1.yaw += (float) (Math.cos(t) * 0.08D);
                this.body.roll += -(float) (Math.cos(t) * 1.0D);
            }
        }

        if (wingState.getCurrentAction() == AnimationAction.WINGFLAP) {
            float flapCycle = flapProgress / 0.2714932F;

            body.pivotY += MathHelper.cos(flapCycle * MathHelper.TAU) * 1.4F;
            rightThigh.pitch = ((float) (rightThigh.pitch + MathHelper.cos(flapCycle * MathHelper.TAU) * 0.08726646324990228D));
            leftThigh.pitch = ((float) (leftThigh.pitch + MathHelper.cos(flapCycle * MathHelper.TAU) * 0.08726646324990228D));
            tail.pitch = ((float) (tail.pitch + MathHelper.cos(flapCycle * MathHelper.TAU) * 0.03490658588512815D));
        }

        this.body.roll = (-roll / 180 * 3.141593F);

        headPitch = (float) MathUtil.boundAngle180Deg(headPitch);
        if (headPitch > 37.16F)
            headPitch = 37.16F;
        else if (headPitch < -56.650002F) {
            headPitch = -56.650002F;
        }
        float pitchFactor = (headPitch + 56.650002F) / 93.800003F;
        this.head.pitch += -0.96F + pitchFactor * -0.1400001F;
        this.neck3.pitch += 0.378F + pitchFactor * -0.528F;
        this.neck2.pitch += 0.4F + pitchFactor * -0.4F;
        this.neck1.pitch += 0.513F + pitchFactor * -0.613F;

        headYaw = (float) MathUtil.boundAngle180Deg(headYaw);
        if (headYaw > 30.5F)
            headYaw = 30.5F;
        else if (headYaw < -30.5F) {
            headYaw = -30.5F;
        }
        float yawFactor = (headYaw + 30.5F) / 61;
        this.head.roll += 0.8F + yawFactor * 2 * -0.8F;
        this.neck3.roll += 0.38F + yawFactor * 2 * -0.38F;
        this.neck2.roll += 0.14F + yawFactor * 2 * -0.14F;
        this.head.yaw += -0.7F + yawFactor * 2 * 0.7F;
        this.neck3.yaw += -0.12F + yawFactor * 2 * 0.12F;
    }

    public void resetSkeleton() {
        getPart().traverse().forEach(ModelPart::resetTransform);
    }
}