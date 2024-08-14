package com.invasion.entity.ai;

import java.util.Optional;

import org.joml.Vector3f;

import com.invasion.entity.EntityIMLiving;
import com.invasion.util.math.IPosition;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class IMMoveHelper extends MoveControl {
    protected final EntityIMLiving entity;

    protected double targetSpeed;
    protected boolean needsUpdate;
    protected boolean isRunning;

    public IMMoveHelper(EntityIMLiving par1EntityLiving) {
        super(par1EntityLiving);
        this.needsUpdate = false;
        this.entity = par1EntityLiving;

        this.speed = (this.targetSpeed = 0.0D);
    }

    public boolean isUpdating() {
        return this.needsUpdate;
    }

    public void setMoveSpeed(float speed) {
        this.speed = speed;
    }

    @Deprecated
    public void setMoveTo(IPosition pos, float speed) {
        moveTo(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), speed);
    }

    public void moveTo(IPosition pos, float speed) {
        moveTo(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), speed);
    }

    @Deprecated
    public void setMoveTo(double x, double y, double z, double speed) {
        moveTo(x, y, z, speed);
    }

    @Override
    public void moveTo(double x, double y, double z, double speed) {
        super.moveTo(x, y, z, speed);
        this.needsUpdate = true;
    }

    @Override
    public void strafeTo(float forward, float sideways) {
        super.strafeTo(forward, sideways);
        this.needsUpdate = true;
    }

    @Override
    public void tick() {
        if (!needsUpdate) {
            entity.setForwardSpeed(0);
            entity.setMoveState(MoveState.STANDING);
            return;
        }

        this.entity.setMoveState(doGroundMovement());
    }

    protected MoveState doGroundMovement() {
        needsUpdate = false;
        targetSpeed = speed;
        boolean isInLiquid = entity.isInsideWaterOrBubbleColumn() || entity.isInLava();
        double dX = targetX - entity.getX();
        double dZ = targetZ - entity.getZ();
        double dY = targetY - (!isInLiquid ? MathHelper.floor(entity.getBoundingBox().minY + 0.5D) : entity.getY());

        Optional<Direction> ladderPos = Optional.empty();
        if (Math.abs(dX) < 0.8D && Math.abs(dZ) < 0.8D && (dY > 0 || entity.isHoldingOntoLadder())) {
            ladderPos = getClimbFace(entity.getBlockPos());
            if (ladderPos.isEmpty()) {
                ladderPos = getClimbFace(entity.getBlockPos().up());
            }
        }

        double dXZSq = dX * dX + dZ * dZ;
        double distanceSquared = dXZSq + dY * dY;
        if ((distanceSquared < 0.01D) && ladderPos.isEmpty()) {
            return MoveState.STANDING;
        }

        if (dXZSq > 0.04D || ladderPos.isPresent()) {
            float newYaw = (float) (Math.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN) - 90;

            if (ladderPos.isPresent()) {
                Vector3f orientation = ladderPos.get().getUnitVector();
                newYaw = (float) (Math.atan2(dZ + orientation.x, dX + orientation.z) * MathHelper.DEGREES_PER_RADIAN)
                        - 90;
            }
            entity.setYaw(correctRotation(entity.getYaw(), newYaw, entity.getTurnRate()));
            double moveSpeed;
            if (distanceSquared >= 0.064D || entity.isSprinting()) {
                moveSpeed = targetSpeed;
            } else {
                moveSpeed = targetSpeed * 0.5D;
            }
            if ((entity.isTouchingWater()) && (moveSpeed < 0.6D)) {
                moveSpeed = 0.6000000238418579D;
            }
            entity.setMovementSpeed((float) moveSpeed);
        }

        double w = Math.max(entity.getWidth() * 0.5F + 1, 1);
        // TODO: Was this added by the modder?
        w = entity.getWidth() * 0.5F + 1;
        if (dY > 0 && (dX * dX + dZ * dZ <= MathHelper.square(w) || isInLiquid)) {
            entity.getJumpControl().setActive();
            if (ladderPos.isPresent()) {
                return MoveState.CLIMBING;
            }
        }
        return MoveState.RUNNING;
    }

    protected float correctRotation(float currentYaw, float newYaw, float turnSpeed) {
        return currentYaw + MathHelper.clamp(MathHelper.subtractAngles(newYaw, currentYaw), -turnSpeed, turnSpeed);
    }

    protected Optional<Direction> getClimbFace(BlockPos pos) {
        BlockState state = entity.getWorld().getBlockState(pos);
        if (state.isIn(BlockTags.CLIMBABLE)) {
            return state.getOrEmpty(Properties.HORIZONTAL_FACING);
        }
        return Optional.empty();
    }
}