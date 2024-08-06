package invmod.common.util;

public record SingleSelection<T>(T object) implements ISelect<T> {
    @Override
    public T selectNext() {
        return this.object;
    }

    @Override
    public void reset() {
    }

    @Override
    public String toString() {
        return this.object.toString();
    }
}