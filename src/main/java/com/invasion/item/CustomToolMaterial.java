package com.invasion.item;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

record CustomToolMaterial (
        TagKey<Block> inverseTag,
          int durability,
          float miningSpeedMultiplier,
          float attackDamage,
          int enchantability,
          Ingredient repairIngredient
  ) implements ToolMaterial {
    public static final CustomToolMaterial INFUSED_GOLD = new CustomToolMaterial(BlockTags.INCORRECT_FOR_GOLD_TOOL, 40, 12, 4, 22, Ingredient.ofItems(Items.GOLD_INGOT));

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return miningSpeedMultiplier;
    }

    @Override
    public float getAttackDamage() {
        return attackDamage;
    }

    @Override
    public TagKey<Block> getInverseTag() {
        return inverseTag;
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient;
    }

  }