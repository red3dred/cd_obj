package invmod.client.render;

import invmod.client.render.animation.Animation;
import invmod.client.render.animation.AnimationAction;
import invmod.client.render.animation.AnimationPhaseInfo;
import invmod.client.render.animation.BonesWings;
import invmod.client.render.animation.Transition;
import invmod.common.InvasionMod;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnimationRegistry {
    private static final AnimationRegistry INSTANCE = new AnimationRegistry();

    private final Map<String, Animation<?>> animationMap = new HashMap<>(4);
    private final Animation<BonesWings> emptyAnim = new Animation<>(BonesWings.class, 1, 1,
            new EnumMap<>(BonesWings.class), List.of(
                    new AnimationPhaseInfo(AnimationAction.STAND, 0.0F, 1.0F, new Transition(AnimationAction.STAND, 1, 0))
            ));

    private AnimationRegistry() { }

    public void clear() {
        animationMap.clear();
    }

    public <T extends Enum<T>> void registerAnimation(String name, Animation<T> animation) {
        if (animationMap.put(name, animation) != null) {
            InvasionMod.LOGGER.warn("Register animation: Name \"" + name + "\" already assigned");
        }
    }

    public <T extends Enum<T>> Animation<T> getAnimation(String name) {
        @SuppressWarnings("unchecked")
        Animation<T> animation = (Animation<T>)animationMap.getOrDefault(name, emptyAnim);
        if (animation == emptyAnim) {
            InvasionMod.LOGGER.warn("Tried to use animation \"" + name + "\" but it doesn't exist");
        }

        return animation;
    }

    public static AnimationRegistry instance() {
        return INSTANCE;
    }
}