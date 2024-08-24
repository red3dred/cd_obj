package com.invasion.nexus.ai.scaffold;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.invasion.nexus.NexusAccess;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ScaffoldList implements Iterable<Scaffold> {
    private List<Scaffold> entries = new ArrayList<>();

    public void load(List<Scaffold> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
    }

    public Scaffold get(int index) {
        return entries.get(index);
    }

    @Nullable
    public Scaffold getAt(BlockPos pos) {
        return entries.stream()
                .filter(scaffold -> scaffold.getNode().contains(pos))
                .findFirst()
                .orElse(null);
    }

    public void tick(World world) {
        entries.removeIf(scaffold -> {
            Vec3d pos = scaffold.getNode().pos().toCenterPos();
            world.addParticle(ParticleTypes.HEART, pos.x, pos.y, pos.z, 0.5D, 0.5D, 0.5D);

            return scaffold.updateStatus();
        });
    }

    public int size() {
        return entries.size();
    }

    public boolean addAll(NexusAccess nexus, List<ScaffoldNode> newScaffolds) {
        newScaffolds = new ArrayList<>(newScaffolds);
        boolean changed = newScaffolds.removeIf(newScaffold -> {
            return entries.stream().anyMatch(existingScaffold -> existingScaffold.merge(newScaffold));
        });
        changed |= entries.addAll(newScaffolds.stream().map(node -> new Scaffold(node, nexus)).toList());
        return changed;
    }

    @Override
    public Iterator<Scaffold> iterator() {
        return entries.iterator();
    }
}
