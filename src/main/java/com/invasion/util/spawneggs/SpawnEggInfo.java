package com.invasion.util.spawneggs;

import net.minecraft.nbt.NbtCompound;

@Deprecated(since ="No longer needed")
public class SpawnEggInfo {

	public final short eggID;
	public final String mobID;
	public final String displayName;
	public final NbtCompound spawnData;
	public final int primaryColor;
	public final int secondaryColor;

	public SpawnEggInfo(short eggID, String mobID, String displayName, NbtCompound spawnData, int primaryColor, int secondaryColor) {
		this.eggID = eggID;
		this.mobID =  mobID;
		this.displayName = displayName;
		this.spawnData = spawnData;
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
	}

	public SpawnEggInfo(short eggID, String mobID, NbtCompound compound, int primaryColor, int secondaryColor) {
		this(eggID, mobID, null, compound, primaryColor, secondaryColor);
	}

}