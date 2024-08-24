package com.invasion.nexus.ai;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.invasion.entity.NexusEntity;
import com.invasion.nexus.Combatant;
import com.invasion.nexus.Nexus;
import com.invasion.nexus.ai.scaffold.Scaffold;
import com.invasion.nexus.ai.scaffold.ScaffoldGenerator;
import com.invasion.nexus.ai.scaffold.ScaffoldList;
import com.invasion.nexus.ai.scaffold.ScaffoldView;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CollisionView;

public class AttackerAI {
    private static final ExecutorService SCAFFOLD_EXECUTOR = Executors.newSingleThreadExecutor();
    private final Nexus nexus;

    private final Long2ObjectMap<Integer> entityDensityData = new Long2ObjectOpenHashMap<>();

    private final ScaffoldList scaffolds = new ScaffoldList();

    private int nextScaffoldCalcTimer;
    private int updateScaffoldTimer;
    private int nextEntityDensityUpdate;

    public AttackerAI(Nexus nexus) {
        this.nexus = nexus;

    }

    public ScaffoldList getScaffolds() {
        return scaffolds;
    }

    public void tick() {
        nextScaffoldCalcTimer = Math.max(0, nextScaffoldCalcTimer - 1);
        if (--updateScaffoldTimer <= 0) {
            updateScaffoldTimer = 40;
            scaffolds.tick(nexus.getWorld());
        }

        if (--nextEntityDensityUpdate <= 0) {
            nextEntityDensityUpdate = 20;
            entityDensityData.clear();
            for (Combatant<?> mob : nexus.getCombatants()) {
                entityDensityData.compute(mob.asEntity().getBlockPos().asLong(), (key, old) -> (old == null ? 1 : old + 1) & ScaffoldView.MOB_DENSITY_FLAG);
            }
        }
    }

    public CollisionView wrapEntityData(CollisionView terrainMap) {
        return new TerrainDataLayer(terrainMap, entityDensityData);
    }

    public CollisionView addScaffoldDataTo(CollisionView view) {
        ScaffoldView terrainMap = ScaffoldView.of(view);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Scaffold scaffold : scaffolds) {
            BlockPos pos = scaffold.getNode().pos();
            for (int y = scaffold.getNode().bottom(); y < scaffold.getNode().top(); y++) {
                terrainMap.addScaffoldPosition(mutable.set(pos.getX(), y, pos.getZ()));
            }
        }
        return view;
    }

    public void requestBuildJob(NexusEntity entity, Consumer<Optional<BlockPos>> callback) {
        if (nextScaffoldCalcTimer > 0 || scaffolds.size() > getScaffoldLimit()) {
            callback.accept(Optional.empty());
        } else {
            nextScaffoldCalcTimer = 200;
            boolean success = scaffolds.addAll(nexus, new ScaffoldGenerator(this).generateScaffolds(entity));
            if (success) {
                callback.accept(scaffolds.getNearest(entity.asEntity().getBlockPos()));
            } else {
                callback.accept(Optional.empty());
            }
        }
    }

    private int getScaffoldLimit() {
        return 2 + nexus.getCurrentWave() / 2;
    }

    public int getScaffoldSpacing() {
        return 90 / (nexus.getCurrentWave() + 10);
    }

    public void readNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        scaffolds.load(compound.getList("scaffolds", NbtElement.COMPOUND_TYPE)
                .stream()
                .map(element -> new Scaffold((NbtCompound) element, nexus))
                .toList()
        );
    }

    public NbtCompound writeNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        NbtList nbttaglist = new NbtList();
        for (Scaffold scaffold : scaffolds) {
            nbttaglist.add(scaffold.toNBT(new NbtCompound()));
        }
        compound.put("scaffolds", nbttaglist);
        return compound;
    }
}