package com.invasion.entity.ai;

import java.util.Optional;

import com.invasion.entity.EntityIMLiving;

import net.minecraft.entity.ai.control.LookControl;

public class FlyingEntityLookControl extends LookControl {
    public FlyingEntityLookControl(EntityIMLiving entity) {
        super(entity);
    }

    @Override
    protected boolean shouldStayHorizontal() {
        return false;
    }

    @Override
    protected Optional<Float> getTargetPitch() {
        return super.getTargetPitch().map(pitch -> pitch + 40);
    }

    @Override
    protected Optional<Float> getTargetYaw() {
        return super.getTargetYaw().map(yaw -> Math.abs(yaw) > 100 ? 0 : yaw / 6F);
    }
}