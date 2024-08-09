package invmod.common.nexus;

public class MobBuilder {
    private static final MobBuilder INSTANCE = new MobBuilder();

    public static MobBuilder getInstance() {
        return INSTANCE;
    }

    private MobBuilder() { }




}