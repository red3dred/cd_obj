package invmod.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FiniteSelectionPool<T> implements ISelect<T> {
    private final Random rand = new Random();
	private final List<Entry<T>> currentPool = new ArrayList<>();
	private int totalAmount;
	private int originalAmount;

	public FiniteSelectionPool<T> addEntry(T entry, int amount) {
		return addEntry(new SingleSelection<>(entry), amount);
	}

	public FiniteSelectionPool<T> addEntry(ISelect<T> entry, int amount) {
		currentPool.add(new Entry<>(entry, amount));
		originalAmount = (totalAmount += amount);
		return this;
	}

	@Override
    public T selectNext() {
		if (totalAmount < 1) {
			regeneratePool();
		}
		float r = rand.nextInt(totalAmount);
		for (Entry<T> entry : currentPool) {
			if (r < entry.amount) {
				totalAmount--;
				return entry.selectNext();
			}

			r -= entry.amount;
		}

		return null;
	}

	@Override
    public FiniteSelectionPool<T> clone() {
		FiniteSelectionPool<T> clone = new FiniteSelectionPool<>();
		clone.currentPool.addAll(currentPool);
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
			s = s + "\n\tEntry " + i + "   Amount: " + currentPool.get(i).initialAmount;
			s = s + "\n\t" + currentPool.get(i).amount;
		}
		return s;
	}

	private void regeneratePool() {
		totalAmount = originalAmount;
		currentPool.forEach(ISelect::reset);
	}

	private static class Entry<T> implements ISelect<T> {
	    ISelect<T> value;
	    int amount;
	    final int initialAmount;

	    Entry(ISelect<T> value, int amount) {
	        this.value = value;
	        this.initialAmount = amount;
	        this.amount = amount;
	    }

        @Override
        public T selectNext() {
            amount--;
            return value.selectNext();
        }

        @Override
        public void reset() {
            amount = initialAmount;
            value.reset();
        }
	}
}
