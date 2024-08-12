package invmod.common.entity;

public enum FlyState {
    FLYING,
    GROUNDED,
    TAKEOFF,
    LANDING,
    TOUCHDOWN,
    CRASHING,
    SWOOPING_P1,
    SWOOPING_P2,
    STRIKING;

    public static final FlyState[] VALUES = values();

    public static FlyState of(int id) {
        return VALUES[id % VALUES.length];
    }
}