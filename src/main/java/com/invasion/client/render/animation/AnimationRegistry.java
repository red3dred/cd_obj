package com.invasion.client.render.animation;

import java.util.HashMap;
import java.util.Map;

import com.invasion.InvasionMod;

public final class AnimationRegistry {
    private static final AnimationRegistry INSTANCE = new AnimationRegistry();

    private final Map<String, Animation<?>> animations = new HashMap<>();

    private AnimationRegistry() { }

    public void clear() {
        animations.clear();
    }

    public <T extends Enum<T>> void register(String name, Animation<T> animation) {
        if (animations.put(name, animation) != null) {
            InvasionMod.LOGGER.warn("Register animation: Name \"" + name + "\" already assigned");
        }
    }

    public <T extends Enum<T>> Animation<T> get(String name) {
        @SuppressWarnings("unchecked")
        Animation<T> animation = (Animation<T>)animations.getOrDefault(name, Animation.EMPTY);
        if (animation == Animation.EMPTY) {
            InvasionMod.LOGGER.warn("Tried to use animation \"" + name + "\" but it doesn't exist");
        }

        return animation;
    }

    public static AnimationRegistry instance() {
        return INSTANCE;
    }
}