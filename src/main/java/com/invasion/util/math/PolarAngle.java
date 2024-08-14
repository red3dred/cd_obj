package com.invasion.util.math;

public record PolarAngle(int angle) implements IPolarAngle {
    @Override
    public int getAngle() {
        return this.angle;
    }
}
