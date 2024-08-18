package com.invasion.entity.ai;

import java.util.Optional;

import org.joml.Vector3f;

import com.invasion.entity.AnimatableEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class IMMoveHelper extends MoveControl {
    private float turnRate = 90;

    public IMMoveHelper(MobEntity entity) {
        super(entity);
    }

    public float getTurnRate() {
        return turnRate;
    }

    public void setTurnRate(float rate) {
        turnRate = rate;
    }

    @Override
    public void tick() {
        State prevState = state;

        if (state == MoveControl.State.STRAFE) {
            Optional<Direction> ladderPos = getClimbFace(entity.getBlockPos()).or(() -> getClimbFace(entity.getBlockPos().up()));
            if (ladderPos.isPresent()) {
                entity.getJumpControl().setActive();
                state = State.WAIT;
                return;
            }
        }

        super.tick();

        if (prevState == State.MOVE_TO) {
            double dX = targetX - entity.getX();
            double dZ = targetZ - entity.getZ();
            double dY = targetY - entity.getY();

            Optional<Direction> ladderPos = Optional.empty();
            if (Math.abs(dX) < 0.8D && Math.abs(dZ) < 0.8D && (dY > 0 || entity.isHoldingOntoLadder())) {
                ladderPos = getClimbFace(entity.getBlockPos()).or(() -> getClimbFace(entity.getBlockPos().up()));
            }

            double dXZSq = dX * dX + dZ * dZ;
            double distanceSquared = dXZSq + dY * dY;
            if ((distanceSquared < 0.01D) && ladderPos.isEmpty()) {
                if (entity instanceof AnimatableEntity ae) {
                    ae.setMoveState(MoveState.STANDING);
                }
            } else if (ladderPos.isPresent()) {
                Vector3f orientation = ladderPos.get().getUnitVector();
                float newYaw = (float) (Math.atan2(dX + orientation.x, dZ + orientation.z) * MathHelper.DEGREES_PER_RADIAN) - 90;
                entity.setYaw(wrapDegrees(entity.getYaw(), newYaw, getTurnRate()));
                if (entity instanceof AnimatableEntity ae) {
                    ae.setMoveState(MoveState.CLIMBING);
                }
            } else if (entity instanceof AnimatableEntity ae) {
                ae.setMoveState(MoveState.RUNNING);
            }
        }
    }

    protected Optional<Direction> getClimbFace(BlockPos pos) {
        BlockState state = entity.getWorld().getBlockState(pos);
        if (state.isIn(BlockTags.CLIMBABLE)) {
            return state.getOrEmpty(Properties.HORIZONTAL_FACING);
        }
        return Optional.empty();
    }
}