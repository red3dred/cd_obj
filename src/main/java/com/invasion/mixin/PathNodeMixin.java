package com.invasion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;

import net.minecraft.entity.ai.pathing.PathNode;

@Mixin(PathNode.class)
abstract class PathNodeMixin implements ActionablePathNode {
    private PathAction action = PathAction.NONE;

    @Override
    public PathAction getAction() {
        return action;
    }

    @Override
    public void setAction(PathAction action) {
        this.action = action;
    }

    @Inject(method = "copyWithNewPosition", at = @At("RETURN"))
    private void invasion_after_copyWithNewPosition(int x, int y, int z, CallbackInfoReturnable<PathNode> info) {
        ((ActionablePathNode)info.getReturnValue()).setAction(action);
    }
}
