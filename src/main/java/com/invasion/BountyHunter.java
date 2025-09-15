package com.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;

/**
 * Cleans up players that somehow get away
 *
 * If the player is not in the world at the time the nexus is destroyed/closed
 * they get sent to the hunter to be killed once they return.
 */
public class BountyHunter extends PersistentState {
    private static final int TICK_RATE = 3500;
    private static final Codec<List<UUID>> DEATH_LIST_CODEC = Uuids.CODEC.listOf();
    private static final Identifier ID = InvasionMod.id("nexus_bounty_hunter");

    public static Type<BountyHunter> getType(ServerWorld world) {
        return new PersistentState.Type<>(() -> new BountyHunter(world), (nbt, lookup) -> new BountyHunter(world, nbt, lookup), DataFixTypes.LEVEL);
    }

    public static BountyHunter of(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(getType(world), ID.toUnderscoreSeparatedString());
    }

    private final List<UUID> players = new ArrayList<>();
    private long time;

    private final ServerWorld world;

    private BountyHunter(ServerWorld world) {
        this.world = world;
    }

    private BountyHunter(ServerWorld world, NbtCompound nbt, WrapperLookup lookup) {
        this(world);
        DEATH_LIST_CODEC.decode(NbtOps.INSTANCE, nbt.get("players")).result().map(Pair::getFirst).ifPresent(players::addAll);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup lookup) {
        DEATH_LIST_CODEC.encodeStart(NbtOps.INSTANCE, players).result().ifPresent(d -> nbt.put("players", d));
        return nbt;
    }

    public void tick() {
        if (players.isEmpty()) {
            return;
        }

        markDirty();

        if (++time % TICK_RATE == 0) {
            for (UUID id : players) {
                PlayerEntity player = world.getPlayerByUuid(id);
                if (player != null) {
                    players.remove(id);
                    player.damage(world.getDamageSources().magic(), 500);
                    player.setHealth(1);
                    world.getServer().getPlayerManager().sendToAll(new GameMessageS2CPacket(Text.literal("Nexus energies caught up to ").append(player.getDisplayName()), false));
                    markDirty();
                }
            }
        }
    }

    public void add(UUID playerId) {
        players.add(playerId);
        markDirty();
    }

}
