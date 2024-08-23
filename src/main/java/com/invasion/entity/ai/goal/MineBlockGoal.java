package com.invasion.entity.ai.goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

import com.invasion.InvasionMod;
import com.invasion.block.BlockMetadata;
import com.invasion.block.InvBlockEntities;
import com.invasion.block.InvBlocks;
import com.invasion.block.NexusBlockEntity;
import com.invasion.entity.pathfinding.IMLandPathNodeMaker;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

public class MineBlockGoal extends Goal {
    private final PathAwareEntity mob;
    private final EntityNavigation navigation;

    private float breakProgress;
    private int prevBreakProgress;
    private Stack<BreakEntry> breakingBlockPos = new Stack<>();
    private Optional<BreakEntry> currentEntry = Optional.empty();

    public MineBlockGoal(PathAwareEntity mob) {
        this.mob = mob;
        navigation = mob.getNavigation();
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return mob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)
                && !navigation.isIdle()
                && ActionablePathNode.getAction(navigation.getCurrentPath().getCurrentNode()) == PathAction.DIG;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void start() {
        breakProgress = 0;
        breakingBlockPos.clear();
        if (canStart()) {
            breakingBlockPos.addAll(getClearRegion(mob, navigation.getCurrentPath().getCurrentNodePos()).distinct().toList());
        }
    }

    @Override
    public boolean shouldContinue() {
        return breakingBlockPos.stream().anyMatch(entry -> !mob.getWorld().isAir(entry.pos())) || currentEntry.isPresent();
    }

    @Override
    public void tick() {
        currentEntry = currentEntry.or(() -> {
            return breakingBlockPos.isEmpty() ? Optional.empty() : Optional.of(breakingBlockPos.pop());
        }).filter(entry -> {
            BlockPos pos = entry.pos();
            BlockState breakingState = mob.getWorld().getBlockState(pos);
            if (breakingState != entry.lastKnownState()) {
                return false;
            }
            mob.swingHand(Hand.MAIN_HAND);
            mob.getLookControl().lookAt(pos.toCenterPos());

            float speed = getDiggingSpeed(mob, breakingState, pos) * 10;
            breakProgress += speed;
            System.out.println(breakProgress + " " + speed);
            if (breakProgress >= 10) {
                mob.getWorld().setBlockBreakingInfo(mob.getId(), pos, -1);
                mob.getWorld().breakBlock(pos, InvasionMod.getConfig().destructedBlocksDrop);
                return false;
            } else {
                if ((int)breakProgress != prevBreakProgress) {
                    prevBreakProgress = (int)breakProgress;
                    if (breakingState.isOf(InvBlocks.NEXUS_CORE)) {
                        mob.getWorld()
                            .getBlockEntity(pos, InvBlockEntities.NEXUS)
                            .map(NexusBlockEntity::getNexus)
                            .ifPresent(nexus -> nexus.damage(mob.getDamageSources().mobAttack(mob), 1));
                    }
                }
                mob.getWorld().setBlockBreakingInfo(mob.getId(), pos, prevBreakProgress);
                if (mob.age % 4 == 0) {
                    mob.getWorld().playSound(null, pos,
                            breakingState.getSoundGroup().getHitSound(),
                            SoundCategory.BLOCKS,
                            (breakingState.getSoundGroup().getVolume() + 1) / 8F,
                            breakingState.getSoundGroup().getPitch() * 0.5F
                    );
                }
            }
            return true;
        });
    }

    @Override
    public void stop() {
        currentEntry = currentEntry.filter(entry -> {
            mob.getWorld().setBlockBreakingInfo(mob.getId(), entry.pos(), -1);
            return false;
        });
    }

    static float getDiggingSpeed(LivingEntity entity, BlockState state, BlockPos pos) {
        ItemStack stack = entity.getMainHandStack();
        float multiplier = stack.getMiningSpeedMultiplier(state);
        if (StatusEffectUtil.hasHaste(entity)) {
            multiplier *= 1 + (StatusEffectUtil.getHasteAmplifier(entity) + 1) * 0.2F;
        }
        if (entity.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            multiplier *= switch(entity.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
        }

        float hardness = state.getHardness(entity.getWorld(), pos);
        float speed = !state.isToolRequired() || stack.isSuitableFor(state) ? 30 : 100;
        return multiplier / hardness / speed;
    }

    static Stream<BreakEntry> getClearRegion(PathAwareEntity mob, BlockPos center) {
        return BlockPos.stream(mob.getDimensions(mob.getPose()).getBoxAt(
                    new BlockPos(center.getX(), mob.getBlockPos().getY(), center.getZ()).toBottomCenterPos()
                ))
                .filter(pos -> IMLandPathNodeMaker.canMineBlock(mob, pos))
                .map(BlockPos::toImmutable)
                .sorted(Comparator.comparing(i -> mob.squaredDistanceTo(i.toCenterPos()) + BlockMetadata.getStrength(i, mob.getWorld().getBlockState(i), mob.getWorld())))
                .map(i -> new BreakEntry(i, mob.getWorld().getBlockState(i)));
    }

    record BreakEntry(BlockPos pos, BlockState lastKnownState) {}
}
