package com.invasion;

public interface Notifiable {
    Notifiable NONE = status -> {};

    void notifyTask(Status status);

    enum Status {
        SUCCESS,
        OUT_OF_RANGE,
        UNMODIFIABLE
    }
}