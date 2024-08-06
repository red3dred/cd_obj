package invmod.common.nexus;

public record EntityConstruct (
        IMEntityType entityType,
        int texture,
        int tier,
        int flavour,
        float scaling,
        int minAngle,
        int maxAngle) {
}