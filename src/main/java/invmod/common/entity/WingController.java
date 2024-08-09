package invmod.common.entity;

import invmod.client.render.animation.AnimationAction;
import invmod.client.render.animation.AnimationController;
import invmod.client.render.animation.AnimationState;

public class WingController implements AnimationController {
    private final EntityIMBird entity;
    private final AnimationState<?> animationFlap;

    private int timeAttacking;
    private float flapEffort = 1;
    private final float[] flapEffortSamples = { 1, 1, 1, 1, 1, 1 };
    private int sampleIndex;

    public WingController(EntityIMBird entity, AnimationState<?> stateObject) {
        this.entity = entity;
        this.animationFlap = stateObject;
    }

    @Override
    public AnimationState<?> getState() {
        return animationFlap;
    }

    @Override
    public void update() {
        AnimationAction currAnimation = animationFlap.getCurrentAction();
        boolean wingAttack = entity.isAttackingWithWings();
        if (!wingAttack)
            timeAttacking = 0;
        else {
            timeAttacking++;
        }
        if (entity.age % 5 == 0) {
            if (++sampleIndex >= flapEffortSamples.length) {
                sampleIndex = 0;
            }
            float sample = entity.getThrustEffort();
            flapEffort -= flapEffortSamples[sampleIndex] / flapEffortSamples.length;
            flapEffort += sample / flapEffortSamples.length;
            flapEffortSamples[sampleIndex] = sample;
        }

        if (entity.getFlyState() != FlyState.GROUNDED) {
            if (currAnimation == AnimationAction.WINGTUCK) {
                ensureAnimation(AnimationAction.WINGSPREAD, 2.2F, true);
            } else if (entity.isThrustOn()) {
                ensureAnimation(AnimationAction.WINGFLAP, 2 * flapEffort, false);
            } else {
                ensureAnimation(AnimationAction.WINGGLIDE, 0.7F, false);
            }
        } else {
            boolean wingsActive = false;
            if (entity.getMoveState() == MoveState.RUNNING) {
                if (currAnimation == AnimationAction.WINGTUCK) {
                    ensureAnimation(AnimationAction.WINGSPREAD, 2.2F, true);
                } else {
                    ensureAnimation(AnimationAction.WINGFLAP, 1, false);
                    if (!wingAttack && currAnimation == AnimationAction.WINGSPREAD && animationFlap.getCurrentAnimationPercent() >= 0.65F) {
                        this.animationFlap.setPaused(true);
                    }
                }
                wingsActive = true;
            }

            if (wingAttack) {
                ensureAnimation(AnimationAction.WINGFLAP, (float) (1D / Math.min(timeAttacking / 40 * 0.6D + 0.4D, 1)), false);
                wingsActive = true;
            }

            if (!wingsActive) {
                ensureAnimation(AnimationAction.WINGTUCK, 1.8F, true);
            }
        }
        this.animationFlap.update();
    }
}