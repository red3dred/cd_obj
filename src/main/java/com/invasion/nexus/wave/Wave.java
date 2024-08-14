package com.invasion.nexus.wave;

import java.util.ArrayList;
import java.util.List;

import com.invasion.nexus.spawns.ISpawnerAccess;

public class Wave {
    private final List<WaveEntry> entries;
    private final int waveTotalTime;
    private final int waveBreakTime;

    private int elapsed;

    public Wave(int waveTotalTime, int waveBreakTime) {
        this(waveTotalTime, waveBreakTime, new ArrayList<>());
    }

    public Wave(int waveTotalTime, int waveBreakTime, List<WaveEntry> entries) {
        this.entries = entries;
        this.waveTotalTime = waveTotalTime;
        this.waveBreakTime = waveBreakTime;
    }

    public int doNextSpawns(int elapsedMillis, ISpawnerAccess spawner) {
        int numberOfSpawns = 0;
        elapsed += elapsedMillis;
        for (WaveEntry entry : entries) {
            if (elapsed >= entry.getTimeBegin() && elapsed < entry.getTimeEnd()) {
                numberOfSpawns += entry.doNextSpawns(elapsedMillis, spawner);
            }
        }
        return numberOfSpawns;
    }

    public int getTimeInWave() {
        return elapsed;
    }

    public int getWaveTotalTime() {
        return waveTotalTime;
    }

    public int getWaveBreakTime() {
        return waveBreakTime;
    }

    public boolean isComplete() {
        return elapsed > waveTotalTime;
    }

    public void resetWave() {
        this.elapsed = 0;
        for (WaveEntry entry : entries) {
            entry.resetToBeginning();
        }
    }

    public void setWaveToTime(int millis) {
        elapsed = millis;
    }

    public int getTotalMobAmount() {
        int total = 0;
        for (WaveEntry entry : entries) {
            total += entry.getAmount();
        }
        return total;
    }
}