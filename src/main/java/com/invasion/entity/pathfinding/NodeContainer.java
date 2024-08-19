package com.invasion.entity.pathfinding;

/**
 * Source: net.minecraft.entity.ai.pathing.PathMinHeap
 *
 * Changed to use our PathNode class and to extend the path length to 1024
 */
public class NodeContainer {
	private PathNode[] pathNodes = new PathNode[1024];
	private int count;

	public PathNode push(PathNode pathpoint) {
		if (pathpoint.heapIndex >= 0) {
			throw new IllegalStateException("OW KNOWS!");
		}
		if (this.count == this.pathNodes.length) {
			PathNode[] apathpoint = new PathNode[this.count << 1];
			System.arraycopy(this.pathNodes, 0, apathpoint, 0, this.count);
			this.pathNodes = apathpoint;
		}
		this.pathNodes[this.count] = pathpoint;
		pathpoint.heapIndex = this.count;
		shiftUp(this.count++);
		return pathpoint;
	}

	public void clear() {
		this.count = 0;
	}

	public PathNode pop() {
		PathNode pathpoint = this.pathNodes[0];
		this.pathNodes[0] = this.pathNodes[(--this.count)];
		this.pathNodes[this.count] = null;
		if (this.count > 0) {
			shiftDown(0);
		}
		pathpoint.heapIndex = -1;
		return pathpoint;
	}

	public void setNodeWeight(PathNode pathpoint, float f) {
		float f1 = pathpoint.heapWeight;
		pathpoint.heapWeight = f;
		if (f < f1) {
		    shiftUp(pathpoint.heapIndex);
		} else {
		    shiftDown(pathpoint.heapIndex);
		}
	}

	private void shiftUp(int i) {
		PathNode pathpoint = this.pathNodes[i];
		float f = pathpoint.heapWeight;

		while (i > 0) {
			int j = i - 1 >> 1;
			PathNode pathpoint1 = this.pathNodes[j];
			if (f >= pathpoint1.heapWeight) {
				break;
			}
			this.pathNodes[i] = pathpoint1;
			pathpoint1.heapIndex = i;
			i = j;
		}

		this.pathNodes[i] = pathpoint;
		pathpoint.heapIndex = i;
	}

	private void shiftDown(int i) {
		PathNode pathpoint = pathNodes[i];
		float f = pathpoint.heapWeight;
		while (true) {
			int j = 1 + (i << 1);
			int k = j + 1;
			if (j >= count) {
				break;
			}
			PathNode pathpoint1 = pathNodes[j];
			float f1 = pathpoint1.heapWeight;
			float f2;
			PathNode pathpoint2;
			if (k >= count) {
				pathpoint2 = null;
				f2 = 1;
			} else {
				pathpoint2 = pathNodes[k];
				f2 = pathpoint2.heapWeight;
			}
			if (f1 < f2) {
				if (f1 >= f) {
					break;
				}
				pathNodes[i] = pathpoint1;

				pathpoint1.heapIndex = i;
				i = j;
			} else {
				if (f2 >= f) {
					break;
				}
				//Unstoppable Custom Testcode
				//this seems to temp fix mobs not being able to spawn.
				if (pathpoint2 == null) {
					break;
				}
				//end Unstoppable Custom Testcode
				pathNodes[i] = pathpoint2;
				pathpoint2.heapIndex = i;
				i = k;
			}
		}
		pathNodes[i] = pathpoint;
		pathpoint.heapIndex = i;
	}

	public boolean isEmpty() {
		return count == 0;
	}
}