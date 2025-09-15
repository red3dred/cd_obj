package com.invasion.nexus.wave;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.invasion.InvasionMod;
import com.invasion.entity.InvEntities;

import net.minecraft.util.Identifier;

public interface EntityPatterns {
    Map<Identifier, PatternType> REGISTRY = new HashMap<>();

    EntityPattern ZOMBIE_T1_ANY = register("zombie_t1_any", new EntityPattern.Builder(InvEntities.ZOMBIE).addTier(1, 1).addFlavour(0, 3).addFlavour(1, 1), 1);
    EntityPattern ZOMBIE_T2_ANY_BASIC = register("zombie_t2_any_basic", new EntityPattern.Builder(InvEntities.ZOMBIE).addTier(2, 1).addFlavour(0, 2).addFlavour(1, 1).addFlavour(2, 0.4F), 1);
    EntityPattern ZOMBIE_T2_PLAIN = register("zombie_t2_plain",  new EntityPattern.Builder(InvEntities.ZOMBIE).addTier(2, 1).addFlavour(0, 1));
    EntityPattern ZOMBIE_T2_TAR = register("zombie_t2_tar", new EntityPattern.Builder(InvEntities.ZOMBIE).addTier(2, 1).addFlavour(2, 1).addTexture(5, 1));
    EntityPattern ZOMBIE_T3_ANY = register("zombie_t3_any", new EntityPattern.Builder(InvEntities.ZOMBIE).addTier(3, 1).addTexture(0, 1));

    EntityPattern ZOMBIE_PIGMAN_T1_ANY = register("zombie_pigman_t1_any", new EntityPattern.Builder(InvEntities.ZOMBIE_PIGMAN).addTier(1, 1).addFlavour(0, 1));
    EntityPattern ZOMBIE_PIGMAN_T2_ANY = register("zombie_pigman_t2_any", new EntityPattern.Builder(InvEntities.ZOMBIE_PIGMAN).addTier(2, 1).addFlavour(0, 1));
    EntityPattern ZOMBIE_PIGMAN_T3_ANY = register("zombie_pigman_t3_any", new EntityPattern.Builder(InvEntities.ZOMBIE_PIGMAN).addTier(3, 1).addFlavour(0, 1));

    EntityPattern SPIDER_T1_ANY = register("spider_t1_any", new EntityPattern.Builder(InvEntities.SPIDER), 0.5F);
    EntityPattern SPIDER_T2_ANY = register("spider_t2_any", new EntityPattern.Builder(InvEntities.SPIDER)
            .addType(InvEntities.JUMPING_SPIDER, 1)
            .addType(InvEntities.QUEEN_SPIDER, 0.5F));
    EntityPattern SPIDER_T3_ANY = register("spider_t3_any", new EntityPattern.Builder(InvEntities.JUMPING_SPIDER)
            .addType(InvEntities.SPIDER, 0.5F)
            .addType(InvEntities.QUEEN_SPIDER, 1));

    EntityPattern PIGMAN_ENGINEER_T1_ANY = register("pigman_engineer_t1_any", new EntityPattern.Builder(InvEntities.PIGMAN_ENGINEER).addTier(1, 1));

    EntityPattern SKELETON_T1_ANY = register("skeleton_t1_any", new EntityPattern.Builder(InvEntities.SKELETON).addTier(1, 1));

    EntityPattern THROWER_T1 = register("thrower_t1_any", new EntityPattern.Builder(InvEntities.THROWER).addTier(1, 1));
    EntityPattern THROWER_T2 = register("thrower_t2_any", new EntityPattern.Builder(InvEntities.THROWER).addTier(2, 1));

    EntityPattern BURROWER = register("burrower", new EntityPattern.Builder(InvEntities.BURROWER).addTier(1, 1));

    EntityPattern CREEPER_T1_BASIC = register("creeper_t1_basic", new EntityPattern.Builder(InvEntities.CREEPER).addTier(1, 1));

    EntityPattern IMP_T1 = register("imp_t1", new EntityPattern.Builder(InvEntities.IMP).addTier(1, 1));

    static EntityPattern register(String name, EntityPattern.Builder builder) {
        return register(name, builder, 0);
    }

    static EntityPattern register(String name, EntityPattern.Builder builder, float spawnWeight) {
        Identifier id = InvasionMod.id(name);
        EntityPattern pattern = builder.build();
        REGISTRY.put(id, new PatternType(id, pattern, spawnWeight));
        return pattern;
    }

    static EntityPattern getPattern(Identifier id) {
        return getKey(id).map(PatternType::pattern).orElse(EntityPatterns.ZOMBIE_T1_ANY);
    }

    static Optional<PatternType> getKey(Identifier id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    static boolean isPatternNameValid(Identifier id) {
        return REGISTRY.containsKey(id);
    }

    record PatternType(Identifier id, EntityPattern pattern, float defaultSpawnWeight) {
        public float getNightMobSpawnWeight() {
            return InvasionMod.getConfig().getPropertyValueFloat("nm-spawnpool1-slot-" + id + "-weight", defaultSpawnWeight);
        }
    }
}
