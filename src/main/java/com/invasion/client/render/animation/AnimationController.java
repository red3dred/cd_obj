package com.invasion.client.render.animation;

public interface AnimationController {
    AnimationState<?> getState();

    void update();


    default void ensureAnimation(AnimationAction action, float animationSpeed, boolean pauseAfterAction) {
        AnimationState<?> state = getState();
        if (state.getNextSetAction() != action) {
            state.setNewAction(action, animationSpeed, pauseAfterAction);
        } else {
            state.setAnimationSpeed(animationSpeed);
            state.setPauseAfterSetAction(pauseAfterAction);
            state.setPaused(false);
        }
    }
}
