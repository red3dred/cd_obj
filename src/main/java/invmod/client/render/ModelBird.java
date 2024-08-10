package invmod.client.render;

import invmod.client.render.animation.BonesWings;
import invmod.client.render.animation.ModelAnimator;
import invmod.common.entity.EntityIMBird;

import java.util.Map;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class ModelBird extends SinglePartEntityModel<EntityIMBird> {
    private ModelAnimator<BonesWings> animationWingFlap;
    private final ModelPart root;

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;

    private final ModelPart leftThigh;
    private final ModelPart rightThigh;

    private final ModelPart[] legParts;

    public ModelBird(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        tail = root.getChild("tail");
        leftThigh = body.getChild("left_thigh");
        rightThigh = body.getChild("right_thigh");
        ModelPart leftLeg = leftThigh.getChild("leg");
        ModelPart rightLeg = rightThigh.getChild("leg");
        legParts = new ModelPart[] {
                leftThigh, rightThigh,
                leftLeg, rightLeg,
                leftLeg.getChild("left_toe"), leftLeg.getChild("right_toe"), leftLeg.getChild("back_toe"),
                rightLeg.getChild("left_toe"), rightLeg.getChild("right_toe"), rightLeg.getChild("back_toe")
        };
        animationWingFlap = AnimationRegistry.instance().<BonesWings>getAnimation("bird_wing_flap").createAnimator(Map.of(
                BonesWings.RIGHT_SHOULDER, body.getChild("right_wing_1"),
                BonesWings.LEFT_SHOULDER, body.getChild("left_wing_1"),
                BonesWings.RIGHT_ELBOW, body.getChild("right_wing_1").getChild("right_wing_2"),
                BonesWings.LEFT_ELBOW, body.getChild("left_wing_1").getChild("left_wing_2")
        ));
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData body = root.addChild("body", ModelPartBuilder.create().uv(24, 0).mirrored().cuboid(-3.5F, 0, -3.5F, 7, 12, 7), ModelTransform.pivot(3.5F, 7, 3.5F));
        body.addChild("right_wing_1", ModelPartBuilder.create().uv(0, 22).cuboid(-7, -1, -1, 7, 9, 1), ModelTransform.pivot(-3.5F, 2, 3.5F))
            .addChild("right_wing_2", ModelPartBuilder.create().uv(16, 24).cuboid(-14, -1, -0.5F, 14, 7, 1), ModelTransform.pivot(-7, 0, -0.5F));
        body.addChild("left_wing_1", ModelPartBuilder.create().uv(0, 22).mirrored().cuboid(0, -1, -1, 7, 9, 1), ModelTransform.pivot(3.5F, 2, 3.5F))
            .addChild("left_wing_2", ModelPartBuilder.create().uv(16, 24).mirrored().cuboid(0, -1, -0.5F, 14, 7, 1), ModelTransform.pivot(7, 0, -0.5F));
        body.addChild("head", ModelPartBuilder.create().uv(2, 0).mirrored().cuboid(-2.5F, -5, -4, 5, 6, 6), ModelTransform.pivot(0, 0.5F, 1.5F))
            .addChild("beak", ModelPartBuilder.create().uv(19, 0).mirrored().cuboid(-0.5F, 0, -2, 1, 2, 2), ModelTransform.pivot(0, -3, -4));
        body.addChild("tail", ModelPartBuilder.create().uv(0, 12).cuboid(-3, 0, 0, 5, 9, 1), ModelTransform.of(0.5F, 12, 2.5F, 0.446143F, 0, 0));

        ModelPartData rightLeg = body
                .addChild("right_thigh", ModelPartBuilder.create().uv(13, 18).cuboid(-1, 0, -1, 2, 2, 2), ModelTransform.pivot(-1.5F, 12, -1))
                .addChild("leg", ModelPartBuilder.create().uv(13, 12).cuboid(-0.5F, 0, -0.5F, 1, 5, 1), ModelTransform.NONE);
        rightLeg.addChild("left_toe", ModelPartBuilder.create().uv(0, 0).cuboid(0, 0, -2, 1, 1, 2), ModelTransform.of(0.2F, 4, 0, 0, -0.1396263F, 0));
        rightLeg.addChild("back_toe", ModelPartBuilder.create().uv(0, 0).cuboid(-0.5F, 0, 0, 1, 1, 2), ModelTransform.of(0, 4, 0, -0.349066F, 0, 0));
        rightLeg.addChild("right_toe", ModelPartBuilder.create().uv(0, 0).cuboid(-1, 0, -2, 1, 1, 2), ModelTransform.of(-0.2F, 4, 0, 0, 0.1396263F, 0));

        ModelPartData leftLeg = body
                .addChild("left_thigh", ModelPartBuilder.create().uv(13, 18).mirrored().cuboid(-1, 0, -1, 2, 2, 2), ModelTransform.pivot(1.5F, 12, -1))
                .addChild("leg", ModelPartBuilder.create().uv(13, 12).mirrored().cuboid(-0.5F, 0, -0.5F, 1, 5, 1), ModelTransform.NONE);
        leftLeg.addChild("left_toe", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(0, 0, -2, 1, 1, 2), ModelTransform.of(0.2F, 4, 0, 0, -0.1396263F, 0));
        leftLeg.addChild("back_toe", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-0.5F, 0, 0, 1, 1, 2), ModelTransform.of(0, 4, 0, -0.349066F, 0, 0));
        leftLeg.addChild("right_toe", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-1, 0, -2, 1, 1, 2), ModelTransform.of(-0.2F, 4, 0, 0, 0.1396263F, 0));
        return TexturedModelData.of(data, 64, 32);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(EntityIMBird entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        body.pitch = 1.570796F - headPitch * MathHelper.RADIANS_PER_DEGREE;
        body.yaw = 0;
        body.roll = -entity.getRotationRoll() * MathHelper.RADIANS_PER_DEGREE;
    }

    @Override
    public void animateModel(EntityIMBird entity, float limbAngle, float limbDistance, float tickDelta) {
        float legSweepProgress = entity.getLegSweepProgress();
        float flapProgress = entity.getWingAnimationState().getCurrentAnimationTimeInterp(tickDelta);
        animationWingFlap.updateAnimation(flapProgress);

        for (ModelPart i : legParts) {
            i.pitch = 0.08726647F * legSweepProgress;
        }

        body.pivotY = (7 + MathHelper.cos(flapProgress * MathHelper.TAU) * 1.4F);
        rightThigh.pitch += MathHelper.cos(flapProgress * MathHelper.TAU) * 0.08726646324990228D;
        leftThigh.pitch += MathHelper.cos(flapProgress * MathHelper.TAU) * 0.08726646324990228D;
        tail.pitch = ((float)(0.2617993956013792D + MathHelper.cos(flapProgress * MathHelper.TAU) * 0.03490658588512815D));
        head.pitch = ((float)(-0.3141592700403172D - MathHelper.cos(flapProgress * MathHelper.TAU) * 0.03490658588512815D));
    }
}