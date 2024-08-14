package com.invasion.entity.pathfinding;

import com.invasion.IPathfindable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class PathCreator implements IPathSource {
	private int searchDepth;
	private int quickFailDepth;
	private int[] nanosUsed;
	private int index;

	public PathCreator() {
		this(200, 50);
	}

	public PathCreator(int searchDepth, int quickFailDepth) {
		this.searchDepth = searchDepth;
		this.quickFailDepth = quickFailDepth;
		this.nanosUsed = new int[6];
		this.index = 0;
	}

	@Override
    public int getSearchDepth() {
		return searchDepth;
	}

	@Override
    public int getQuickFailDepth() {
		return quickFailDepth;
	}

	@Override
    public void setSearchDepth(int depth) {
		searchDepth = depth;
	}

	@Override
    public void setQuickFailDepth(int depth) {
		quickFailDepth = depth;
	}

	@Override
    public Path createPath(IPathfindable entity, BlockPos from, BlockPos to, float targetRadius, float maxSearchRange, BlockView terrainMap) {
		final long time = System.nanoTime();
		final Path path = PathfinderIM.INSTANCE.createPath(entity, from, to, targetRadius, maxSearchRange, terrainMap, searchDepth, quickFailDepth);
		nanosUsed[index = (index + 1) % nanosUsed.length] = (int) (System.nanoTime() - time);
		return path;
	}
}