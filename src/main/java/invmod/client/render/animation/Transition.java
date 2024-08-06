package invmod.client.render.animation;

public record Transition (
        AnimationAction newAction,
        float sourceTime,
        float destTime
) {
}