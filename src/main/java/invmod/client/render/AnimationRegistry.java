package invmod.client.render;

import invmod.client.render.animation.Animation;
import invmod.client.render.animation.AnimationAction;
import invmod.client.render.animation.AnimationPhaseInfo;
import invmod.client.render.animation.BonesWings;
import invmod.client.render.animation.Transition;
import invmod.common.mod_Invasion;
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
        if (!this.animationMap.containsKey(name)) {
            this.animationMap.put(name, animation);
            return;
        }
        mod_Invasion.log("Register animation: Name \"" + name + "\" already assigned");
    }

    public Animation<?> getAnimation(String name) {
        Animation<?> animation = animationMap.getOrDefault(name, emptyAnim);
        if (animation == emptyAnim) {
            mod_Invasion.log("Tried to use animation \"" + name + "\" but it doesn't exist");
        }

        return animation;
    }

    public static AnimationRegistry instance() {
        return INSTANCE;
    }
}