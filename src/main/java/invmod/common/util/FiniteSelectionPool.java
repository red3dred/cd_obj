package invmod.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FiniteSelectionPool<T> implements ISelect<T> {
    private final Random rand = new Random();
	private final List<Pair<ISelect<T>, Integer>> currentPool = new ArrayList<>();
	private final List<Integer> originalPool = new ArrayList<>();
	private int totalAmount;
	private int originalAmount;

	public FiniteSelectionPool<T> addEntry(T entry, int amount) {
		return addEntry(new SingleSelection<>(entry), amount);
	}

	public FiniteSelectionPool<T> addEntry(ISelect<T> entry, int amount) {
		currentPool.add(new Pair<>(entry, Integer.valueOf(amount)));
		originalPool.add(amount);
		originalAmount = (totalAmount += amount);
		return this;
	}

	@Override
    public T selectNext() {
		if (totalAmount < 1) {
			regeneratePool();
		}
		float r = rand.nextInt(totalAmount);
		for (Pair<ISelect<T>, Integer> entry : currentPool) {
			int amountLeft = entry.getVal2();
			if (r < amountLeft) {
				entry.setVal2(amountLeft - 1);
				totalAmount -= 1;
				return entry.getVal1().selectNext();
			}

			r -= amountLeft;
		}

		return null;
	}

	@Override
    public FiniteSelectionPool<T> clone() {
		FiniteSelectionPool<T> clone = new FiniteSelectionPool<>();
		clone.currentPool.addAll(currentPool);
		clone.originalPool.addAll(originalPool);
		clone.totalAmount = totalAmount;
		clone.originalAmount = originalAmount;
		return clone;
	}

	@Override
    public void reset() {
		regeneratePool();
	}

	@Override
    public String toString() {
		String s = "FiniteSelectionPool@" + Integer.toHexString(hashCode()) + "#Size=" + currentPool.size();
		for (int i = 0; i < currentPool.size(); i++) {
			s = s + "\n\tEntry " + i + "   Amount: " + originalPool.get(i);
			s = s + "\n\t" + currentPool.get(i).getVal1().toString();
		}
		return s;
	}

	private void regeneratePool() {
		totalAmount = originalAmount;
		for (int i = 0; i < currentPool.size(); i++) {
			currentPool.get(i).setVal2(originalPool.get(i));
		}
	}

	record Entry<T>(ISelect<T> value, int amount) {}
}
