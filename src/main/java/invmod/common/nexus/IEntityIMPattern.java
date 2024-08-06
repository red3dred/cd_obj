package invmod.common.nexus;

public interface IEntityIMPattern {
    EntityConstruct generateEntityConstruct();

    EntityConstruct generateEntityConstruct(int minAngle, int maxAngle);
}