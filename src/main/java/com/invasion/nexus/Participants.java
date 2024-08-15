package com.invasion.nexus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.invasion.BountyHunter;
import com.invasion.InvasionMod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

public class Participants {
    private final Map<UUID, Entry> entries = new HashMap<>();

    private final Nexus nexus;

    public Participants(Nexus nexus) {
        this.nexus = nexus;
    }

    public void bindPlayers(Box arena) {
        long now = System.currentTimeMillis();
        for (PlayerEntity player : nexus.getWorld().getEntitiesByClass(PlayerEntity.class, arena, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
            Entry entry = entries.get(player.getUuid());

            entries.compute(player.getUuid(), (id, oldEntry) -> {
                if (entry == null) {
                    sendNotice("invmod.message.nexus.lifenowbound", Formatting.GREEN + player.getDisplayName().getString() + (player.getDisplayName().getString().toLowerCase().endsWith("s") ? "'" : "'s"));
                    return new Entry(now, player.getUuid());
                }

                if (now - entry.time > INexusAccess.BIND_EXPIRE_TIME) {
                    entry.time = now;
                    sendNotice("invmod.message.nexus.lifenowbound", Formatting.GREEN + player.getDisplayName().getString() + (player.getDisplayName().getString().toLowerCase().endsWith("s") ? "'" : "'s"));
                }
                return entry;
            });
        }
    }

    public void sendWarning(String translationKey, Object...params) {
        sendMessage(Formatting.RED, translationKey, params);
    }

    public void sendNotice(String translationKey, Object...params) {
        sendMessage(Formatting.DARK_GREEN, translationKey, params);
    }

    public void sendMessage(Formatting color, String translationKey, Object...params) {
        for (Entry entry : entries.values()) {
            PlayerEntity player = entry.getEntity();
            if (player != null) {
                player.sendMessage(Text.translatable(translationKey, params).formatted(color));
            }
        }
    }

    public void playSoundForBoundPlayers(SoundEvent sound) {
        playSoundForBoundPlayers(sound, 1, 1);
    }

    public void playSoundForBoundPlayers(SoundEvent sound, float volume, float pitch) {
        for (Entry entry : entries.values()) {
            try {
                PlayerEntity player = entry.getEntity();
                if (player != null) {
                    player.getWorld().playSound(null, player.getBlockPos(), sound, SoundCategory.AMBIENT, volume, pitch);
                }
            } catch (Exception e) {
                InvasionMod.LOGGER.error("Problem while trying to play sound " + sound + " at player " + entry.id, e);
            }
        }
    }

    public void release() {
        long time = System.currentTimeMillis();
        for (Entry entry : entries.values()) {
            if (time - entry.time < INexusAccess.BIND_EXPIRE_TIME) {
                PlayerEntity player = entry.getEntity();
                if (player != null) {
                    player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.AMBIENT, 4, 1);
                    player.damage(player.getWorld().getDamageSources().magic(), 500);
                } else if (nexus.getWorld() instanceof ServerWorld sw) {
                    BountyHunter.of(sw).add(entry.id);
                }
            }
        }

        entries.clear();
    }

    public void readNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        entries.clear();
        compound.getList("entries", NbtElement.COMPOUND_TYPE).forEach(el -> {
            Entry entry = new Entry((NbtCompound)el);
            entries.put(entry.id, entry);
        });
    }

    public NbtCompound writeNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        NbtList entries = new NbtList();
        for (Entry entry : this.entries.values()) {
            entries.add(entry.writeNbt(new NbtCompound(), lookup));
        }
        compound.put("entries", entries);
        return compound;
    }

    @Override
    public String toString() {
        String boundPlayers = Formatting.AQUA + "";
        for (Entry entry : entries.values()) {
            PlayerEntity player = entry.getEntity();
            if (player != null) {
                boundPlayers += player.getGameProfile().getName() + Formatting.DARK_AQUA + ", " + Formatting.AQUA;
            }
        }
        return boundPlayers.substring(0, boundPlayers.length() - 4);
    }

    private class Entry {
        long time;
        private final UUID id;
        @Nullable
        private PlayerEntity entity;

        public Entry(long time, UUID playerId) {
            this.time = time;
            id = playerId;
        }

        public Entry(NbtCompound compound) {
            this(compound.getLong("time"), compound.getUuid("id"));
        }

        public PlayerEntity getEntity() {
            if (entity == null) {
                entity = nexus.getWorld().getPlayerByUuid(id);
            }
            return entity;
        }

        public NbtCompound writeNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
            compound.putUuid("uuid", id);
            compound.putLong("time", time);
            return compound;
        }
    }
}
