package invmod.client.render.animation;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public record AnimationPhaseInfo (
        AnimationAction action,
        float timeBegin,
        float timeEnd,
        Transition defaultTransition,
        Map<AnimationAction, Transition> transitions
    ) {

    public AnimationPhaseInfo(AnimationAction action, float timeBegin, float timeEnd, Transition defaultTransition) {
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
}