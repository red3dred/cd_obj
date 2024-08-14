package com.invasion.entity.animation;

import com.invasion.client.render.animation.AnimationAction;
import com.invasion.client.render.animation.AnimationController;
import com.invasion.client.render.animation.AnimationState;
import com.invasion.entity.EntityIMBird;
import com.invasion.entity.ai.FlyState;
import com.invasion.entity.ai.MoveState;

import net.minecraft.util.math.MathHelper;

public class LegController implements AnimationController {
    private final EntityIMBird entity;
    private final AnimationState<?> animationRun;

    public LegController(EntityIMBird entity, AnimationState<?> stateObject) {
        this.entity = entity;
        animationRun = stateObject;
    }

    @Override
    public AnimationState<?> getState() {
        return animationRun;
    }

    @Override
    public void update() {
        AnimationAction currAnimation = animationRun.getCurrentAction();
        if (entity.getMoveState() == MoveState.RUNNING) {
            double dX = entity.getX() - entity.lastRenderX;
            double dZ = entity.getZ() - entity.lastRenderZ;
            double dist = Math.sqrt(dX * dX + dZ * dZ);
            float speed = 0.2F + (float) dist * 1.3F;

            if (animationRun.getNextSetAction() != AnimationAction.RUN) {
                if (dist >= MathHelper.EPSILON) {
                    if (currAnimation == AnimationAction.STAND) {
                        ensureAnimation(AnimationAction.STAND_TO_RUN, speed, false);
                    } else if (currAnimation == AnimationAction.STAND_TO_RUN) {
                        ensureAnimation(AnimationAction.RUN, speed, false);
                    } else {
                        ensureAnimation(AnimationAction.STAND, 1, true);
                    }
                }
            } else {
                animationRun.setAnimationSpeed(speed);
                if (dist < MathHelper.EPSILON) {
                    ensureAnimation(AnimationAction.STAND, 0.2F, true);
                }
            }
        } else if (entity.getMoveState() == MoveState.STANDING) {
            ensureAnimation(AnimationAction.STAND, 1, true);
        } else if (entity.getMoveState() == MoveState.FLYING) {
            if (entity.getClawsForward()) {
                if (currAnimation == AnimationAction.STAND) {
                    ensureAnimation(AnimationAction.LEGS_CLAW_ATTACK_P1, 1.5F, true);
                } else if (animationRun.getNextSetAction() != AnimationAction.LEGS_CLAW_ATTACK_P1) {
                    ensureAnimation(AnimationAction.STAND, 1.5F, true);
                }
            } else if ((entity.getFlyState() == FlyState.FLYING || entity.getFlyState() == FlyState.LANDING) && currAnimation != AnimationAction.LEGS_RETRACT) {
                if (currAnimation == AnimationAction.STAND) {
                    ensureAnimation(AnimationAction.LEGS_RETRACT, 1, true);
                } else if (currAnimation == AnimationAction.LEGS_CLAW_ATTACK_P1) {
                    ensureAnimation(AnimationAction.LEGS_CLAW_ATTACK_P2, 1, true);
                } else {
                    ensureAnimation(AnimationAction.STAND, 1, true);
                }
            }
        }

        animationRun.update();
    }

}