package invmod.common.entity;

import invmod.client.render.animation.AnimationAction;
import invmod.client.render.animation.AnimationController;
import invmod.client.render.animation.AnimationState;

public class MouthController implements AnimationController {
    private final AnimationState<?> mouthState;
    private int mouthOpenTime;

    public MouthController(EntityIMBird entity, AnimationState<?> stateObject) {
        mouthState = stateObject;
    }

    @Override
    public AnimationState<?> getState() {
        return mouthState;
    }

    @Override
    public void update() {
        if (this.mouthOpenTime > 0) {
            this.mouthOpenTime--;
            ensureAnimation(AnimationAction.MOUTH_OPEN, 1, true);
        } else {
            ensureAnimation(AnimationAction.MOUTH_CLOSE, 1, true);
        }
        mouthState.update();
    }

    public void setMouthState(int timeOpen) {
        this.mouthOpenTime = timeOpen;
    }
}