package com.invasion.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.invasion.InvasionMod;

public class RandomSelectionPool<T> implements ISelect<T> {
    private final Random rand = new Random();
    private final List<Entry<T>> pool = new ArrayList<>();
    private float totalWeight;

    public RandomSelectionPool<T> addEntry(T entry, float weight) {
        return addEntry(new SingleSelection<>(entry), weight);
    }

    public RandomSelectionPool<T> addEntry(ISelect<T> entry, float weight) {
        pool.add(new Entry<>(entry, weight));
        totalWeight += weight;
        return this;
    }

    @Override
    public T selectNext() {
        float r = rand.nextFloat() * totalWeight;
        for (Entry<T> entry : pool) {
            if (r < entry.weight()) {
                return entry.value().selectNext();
            }

            r -= entry.weight();
        }

        if (pool.size() > 0) {
            InvasionMod.log("RandomSelectionPool invalid setup or rounding error. Failing safe.");
            return pool.get(0).value().selectNext();
        }
        return null;
    }

    @Override
    public RandomSelectionPool<T> clone() {
        RandomSelectionPool<T> clone = new RandomSelectionPool<>();
        clone.pool.addAll(pool);
        return clone;
    }

    @Override
    public void reset() {
    }

    @Override
    public String toString() {
        String s = "RandomSelectionPool@" + Integer.toHexString(hashCode()) + "#Size=" + pool.size();
        for (int i = 0; i < pool.size(); i++) {
            s = s + "\n\tEntry " + i + "   Weight: " + pool.get(i).weight() + "\n\t" + pool.get(i).value();
        }
        return s;
    }

    record Entry<T>(ISelect<T> value, float weight) {}
}