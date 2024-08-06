package invmod.client.render.animation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public record Animation<T extends Enum<T>>(
        Class<T> skeletonType,
        float animationPeriod,
        float baseSpeed,
        EnumMap<T, List<KeyFrame>> keyframes,
        List<AnimationPhaseInfo> phases) {
    public float getAnimationPeriod() {
        return this.animationPeriod;
    }

    public float getBaseSpeed() {
        return this.baseSpeed;
    }

    public List<AnimationPhaseInfo> getAnimationPhases() {
        return Collections.unmodifiableList(this.phases);
    }

    public Class<T> getSkeletonType() {
        return this.skeletonType;
    }

    @Nullable
    public List<KeyFrame> getKeyFramesFor(T skeletonPart) {
        return this.keyframes.getOrDefault(skeletonPart, null);
    }
}