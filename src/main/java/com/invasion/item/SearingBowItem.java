package com.invasion.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class SearingBowItem extends BowItem {
    static final float SEARING_ACTIVATION_PULL_PERCENTAGE = 3.8F;
    public SearingBowItem(Settings settings) {
        super(settings);
    }

    public static float getUncappedPullProgress(int useTicks) {
        float f = useTicks / 20F;
        return (f * f + f * 2) / 3F;
    }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        super.shoot(shooter, projectile, index, speed, divergence, yaw, target);
        float pullProgress = getUncappedPullProgress(getMaxUseTime(getDefaultStack(), shooter) - shooter.getItemUseTimeLeft());
        shooter.sendMessage(Text.literal(pullProgress + ""));
        if (pullProgress > SEARING_ACTIVATION_PULL_PERCENTAGE) {
            projectile.setFireTicks(100);
            if (projectile instanceof PersistentProjectileEntity p) {
                p.setDamage((p.getDamage() + 1) * 1.5F + 1);
            }
        }
    }

    @Override
    protected int getWeaponStackDamage(ItemStack projectile) {
        return 0;
    }
}