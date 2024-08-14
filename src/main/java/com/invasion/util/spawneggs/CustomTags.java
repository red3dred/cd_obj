package com.invasion.util.spawneggs;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

@Deprecated
public interface CustomTags {

	static NbtCompound poweredCreeper() {
		NbtCompound tag = new NbtCompound();
		tag.putByte("powered", (byte) 1);
		return tag;
	}

	static NbtCompound IMZombie_T1() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 0);
		tag.putInt("tier", 1);
		return tag;
	}


	static NbtCompound IMZombie_T2() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 0);
		tag.putInt("tier", 2);
		return tag;
	}

	static NbtCompound IMZombie_T2_tar() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 2);
		tag.putInt("tier", 2);
		return tag;
	}

	static NbtCompound IMZombie_T3() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 0);
		tag.putInt("tier", 3);
		return tag;
	}

	static NbtCompound IMSpider_T1_baby() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 1);
		tag.putInt("tier", 1);
		return tag;
	}

	static NbtCompound IMSpider_T2() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 0);
		tag.putInt("tier", 2);
		return tag;
	}

	static NbtCompound IMSpider_T2_mother() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 1);
		tag.putInt("tier", 2);
		return tag;
	}

	static NbtCompound IMThrower_T2() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("tier", 2);
		return tag;
	}

	static NbtCompound IMZombiePigman_T1() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 1);
		tag.putInt("tier", 1);
		return tag;
	}

	static NbtCompound IMZombiePigman_T2() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 1);
		tag.putInt("tier", 2);
		return tag;
	}

	static NbtCompound IMZombiePigman_T3() {
		NbtCompound tag = new NbtCompound();
		tag.putInt("flavour", 1);
		tag.putInt("tier", 3);
		return tag;
	}

	static NbtCompound witherSkeleton() {
		NbtCompound tag = new NbtCompound();
		tag.putByte("SkeletonType", (byte) 1);
		NbtList list = new NbtList();
		NbtCompound swordItem = createItemTag((byte) 1, (short) 0, (short) 272);
		list.add(swordItem);
		for (int i = 0; i < 4; ++i)
			list.add(new NbtCompound());
		tag.put("Equipment", list);
		return tag;
	}

	static NbtCompound villagerZombie() {
		NbtCompound tag = new NbtCompound();
		tag.putByte("IsVillager", (byte)1);
		return tag;
	}

	static NbtCompound babyZombie() {
		NbtCompound tag = new NbtCompound();
		tag.putByte("IsBaby", (byte)1);
		return tag;
	}

	static NbtCompound horseType(int type) {
		NbtCompound tag = new NbtCompound();
		tag.putInt("Type", type);
		return tag;
	}

	static NbtCompound createItemTag(byte count, short damage, short id) {
		NbtCompound item = new NbtCompound();
		item.putByte("Count", count);
		item.putShort("Damage", damage);
		item.putShort("id", id);
		return item;
	}

	static NbtCompound getEntityTag(String entityID) {
		NbtCompound tag = new NbtCompound();
		tag.putString("id", entityID);
		return tag;
	}

	static NbtCompound ridingTag(NbtCompound ridden) {
		NbtCompound tag = new NbtCompound();
		tag.put("Riding", ridden);
		return tag;
	}

	static NbtCompound spiderJockey(boolean wither) {
		NbtCompound skele = (wither) ? witherSkeleton() : new NbtCompound();
		skele.put("Riding", getEntityTag("Spider"));
		return skele;
	}

	static NbtCompound chickenJockey(boolean villager) {
		NbtCompound zomb = babyZombie();
		if (villager)
			zomb.putByte("IsVillager", (byte)1);
		zomb.put("Riding", getEntityTag("Chicken"));
		return zomb;
	}


}