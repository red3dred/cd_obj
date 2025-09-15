package com.invasion.entity;

public interface Stunnable {
    boolean stun(int maxTicks);

    boolean isStunned();
}
