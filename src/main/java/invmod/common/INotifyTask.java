package invmod.common;

public interface INotifyTask {
    INotifyTask NONE = status -> {};

    void notifyTask(Status status);

    enum Status {
        SUCCESS,
        OUT_OF_RANGE,
        UNMODIFIABLE
    }
}