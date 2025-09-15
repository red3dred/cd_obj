package com.invasion.client.render.animation;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public record Phase (
        AnimationAction action,
        float timeBegin,
        float timeEnd,
        Transition defaultTransition,
        Map<AnimationAction, Transition> transitions
    ) {

    public Phase(AnimationAction action, float timeBegin, float timeEnd, Map<AnimationAction, Transition> transitions) {
        this(action, timeBegin, timeEnd, transitions.get(action), transitions);
    }

    public Phase(AnimationAction action, float timeBegin, float timeEnd, Transition defaultTransition) {
        this(action, timeBegin, timeEnd, defaultTransition, new HashMap<>(1));
        transitions.put(defaultTransition.newAction(), defaultTransition);
    }

    public boolean hasTransition(AnimationAction newAction) {
        return transitions.containsKey(newAction);
    }

    @Nullable
    public Transition getTransition(AnimationAction newAction) {
        return transitions.get(newAction);
    }

    public static class Builder {
        private final AnimationAction action;
        private final float timeBegin;
        private final float timeEnd;
        private @Nullable Transition defaultTransition;
        private final Map<AnimationAction, Transition> transitions = new HashMap<>();

        public Builder(AnimationAction action, float timeBegin, float timeEnd) {
            this.action = action;
            this.timeBegin = timeBegin;
            this.timeEnd = timeEnd;
        }

        public Builder defaultTransition(AnimationAction defaultAction, float start, float end) {
            return defaultTransition(defaultAction, new Transition(defaultAction, start, end));
        }

        public Builder transition(AnimationAction action, float start, float end) {
            return transition(action, new Transition(action, start, end));
        }

        public Builder defaultTransition(AnimationAction defaultAction, Transition defaultTransition) {
            this.defaultTransition = defaultTransition;
            return transition(defaultAction, defaultTransition);
        }

        public Builder transition(AnimationAction action, Transition transition) {
            transitions.put(action, transition);
            return this;
        }

        public Phase build() {
            return new Phase(action, timeBegin, timeEnd, defaultTransition, Map.copyOf(transitions));
        }
    }
}