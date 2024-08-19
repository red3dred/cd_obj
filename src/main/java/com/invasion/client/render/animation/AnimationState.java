package com.invasion.client.render.animation;

import org.jetbrains.annotations.Nullable;

public class AnimationState<T extends Enum<T>> {
    private Animation<T> animation;
    private float currentTime;
    private float animationSpeed;
    private boolean pauseAtTransition;
    private boolean pauseAfterSetAction;
    private boolean isPaused;

    @Nullable
    private Phase currentPhase;
    private Transition nextTransition;

    private AnimationAction setAction;

    public AnimationState(Animation<T> animation) {
        this(animation, 0.0F);
    }

    public AnimationState(Animation<T> animation, float startTime) {
        this.animation = animation;
        this.currentTime = startTime;
        this.animationSpeed = animation.speed();
        updatePhase(currentTime);
        nextTransition = currentPhase.defaultTransition();
        setAction = nextTransition.newAction();
    }

    public AnimationState<T> setNewAction(AnimationAction action) {
        setAction = action;
        updateTransition(action);
        setPauseAfterCurrentAction(false);
        setPauseAfterSetAction(false);
        setPaused(false);
        return this;
    }

    public void setNewAction(AnimationAction action, float animationSpeedFactor, boolean pauseAfterAction) {
        setNewAction(action);
        setAnimationSpeed(animationSpeedFactor);
        setPauseAfterSetAction(pauseAfterAction);
    }

    public void setPauseAfterCurrentAction(boolean shouldPause) {
        pauseAtTransition = shouldPause;
    }

    public void setPauseAfterSetAction(boolean shouldPause) {
        pauseAfterSetAction = shouldPause;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public void update() {
        if (isPaused) {
            return;
        }
        currentTime += animationSpeed;
        if (currentTime >= nextTransition.sourceTime()) {
            if (setAction == currentPhase.action() && pauseAfterSetAction) {
                pauseAfterSetAction = false;
                pauseAtTransition = true;
            }

            if (!pauseAtTransition) {
                float overflow = currentTime - nextTransition.sourceTime();
                currentTime = nextTransition.destTime();
                updatePhase(currentTime);
                float phaseLength = currentPhase.timeEnd() - currentPhase.timeBegin();
                overflow = Math.min(overflow, phaseLength);
                updateTransition(setAction);
                currentTime += overflow;
                isPaused = false;
            } else {
                currentTime = nextTransition.sourceTime();
                isPaused = true;
            }
        }
    }

    public AnimationAction getNextSetAction() {
        return setAction;
    }

    public AnimationAction getCurrentAction() {
        return currentPhase.action();
    }

    public float getCurrentAnimationTime() {
        return currentTime;
    }

    public float getCurrentAnimationTimeInterp(float tickDelta) {
        if (isPaused) {
            tickDelta = 0;
        }
        float frameTime = currentTime + tickDelta * animationSpeed;
        if (frameTime < nextTransition.sourceTime()) {
            return frameTime;
        }

        float overFlow = frameTime - nextTransition.sourceTime();
        float phaseLength = currentPhase.timeEnd() - currentPhase.timeBegin();
        if (overFlow > phaseLength) {
            overFlow = phaseLength;
        }
        return nextTransition.destTime() + overFlow;
    }

    public float getCurrentAnimationPercent() {
        return (currentTime - currentPhase.timeBegin()) / (currentPhase.timeEnd() - currentPhase.timeBegin());
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public Transition getNextTransition() {
        return nextTransition;
    }

    public void setAnimationSpeed(float speedFactor) {
        animationSpeed = animation.speed() * speedFactor;
    }

    private boolean updateTransition(AnimationAction action) {
        if (currentPhase.hasTransition(action)) {
            nextTransition = currentPhase.getTransition(action);
            if (currentTime > nextTransition.sourceTime()) {
                nextTransition = currentPhase.defaultTransition();
                return false;
            }
        } else {
            nextTransition = currentPhase.defaultTransition();
        }
        return true;
    }

    private void updatePhase(float time) {
        currentPhase = findPhase(time);
        if (currentPhase == null) {
            currentTime = 0;
            currentPhase = animation.phases().get(0);
        }
    }

    @Nullable
    private Phase findPhase(float time) {
        for (Phase phase : animation.phases()) {
            if ((phase.timeBegin() <= time) && (phase.timeEnd() > time)) {
                return phase;
            }
        }
        return null;
    }
}