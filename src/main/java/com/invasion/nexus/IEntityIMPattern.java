package com.invasion.nexus;

public interface IEntityIMPattern {
    EntityConstruct generateEntityConstruct();

    EntityConstruct generateEntityConstruct(int minAngle, int maxAngle);
}