package invmod.common.nexus;

import invmod.common.util.RandomSelectionPool;

public class EntityPattern implements IEntityIMPattern {
    private static final int DEFAULT_TIER = 1;
    private static final int DEFAULT_FLAVOUR = 0;
    private static final int OPEN_TEXTURE = 0;
    private static final int OPEN_SCALING = 0;

    private final IMEntityType entityType;
    private final RandomSelectionPool<Integer> tierPool = new RandomSelectionPool<>();
    private final RandomSelectionPool<Integer> texturePool = new RandomSelectionPool<>();
    private final RandomSelectionPool<Integer> flavourPool = new RandomSelectionPool<>();

    public EntityPattern(IMEntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public EntityConstruct generateEntityConstruct() {
        return generateEntityConstruct(-180, 180);
    }

    @Override
    public EntityConstruct generateEntityConstruct(int minAngle, int maxAngle) {
        Integer tier = tierPool.selectNext();
        Integer texture = texturePool.selectNext();
        Integer flavour = flavourPool.selectNext();
        return new EntityConstruct(this.entityType,
                tier == null ? DEFAULT_TIER : tier,
                texture == null ? OPEN_TEXTURE : texture,
                flavour == null ? DEFAULT_FLAVOUR : flavour, OPEN_SCALING, minAngle, maxAngle);
    }

    public EntityPattern addTier(int tier, float weight) {
        tierPool.addEntry(tier, weight);
        return this;
    }

    public EntityPattern addTexture(int texture, float weight) {
        texturePool.addEntry(texture, weight);
        return this;
    }

    public EntityPattern addFlavour(int flavour, float weight) {
        flavourPool.addEntry(flavour, weight);
        return this;
    }

    @Override
    public String toString() {
        return "EntityIMPattern@" + Integer.toHexString(hashCode()) + "#" + entityType;
    }
}