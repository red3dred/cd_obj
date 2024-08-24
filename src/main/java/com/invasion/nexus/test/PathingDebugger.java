package com.invasion.nexus.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.stream.IntStreams;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.custom.DebugPathCustomPayload;
import net.minecraft.util.math.BlockPos;

public class PathingDebugger {

    public static void sendPathToClients(Entity sender, @Nullable Path path, float scale) {
        sender.getServer().getPlayerManager().sendToAll(new CustomPayloadS2CPacket(new DebugPathCustomPayload(sender.getId(), createDebuggablePath(path), scale)));
    }

    /*
     Pay no mind to what's below. Enjoy this cute little cottage instead
                         (
                            )
                        (            ./\.
                     |^^^^^^^^^|   ./LLLL\.
                     |`.'`.`'`'| ./LLLLLLLL\.
                     |.'`'.'`.'|/LLLL/^^\LLLL\.
                     |.`.''``./LLLL/^ () ^\LLLL\.
                     |.'`.`./LLLL/^  =   = ^\LLLL\.
                     |.`../LLLL/^  _.----._  ^\LLLL\.
                     |'./LLLL/^ =.' ______ `.  ^\LLLL\.
                     |/LLLL/^   /|--.----.--|\ = ^\LLLL\.
                   ./LLLL/^  = |=|__|____|__|=|    ^\LLLL\.
                 ./LLLL/^=     |*|~~|~~~~|~~|*|   =  ^\LLLL\.
               ./LLLL/^        |=|--|----|--|=|        ^\LLLL\.
             ./LLLL/^      =   `-|__|____|__|-' =        ^\LLLL\.
            /LLLL/^   =         `------------'        =    ^\LLLL\
            ~~|.~       =        =      =          =         ~.|~~
              ||     =      =      = ____     =         =     ||
              ||  =               .-'    '-.        =         ||
              ||     _..._ =    .'  .-()-.  '.  =   _..._  =  ||
              || = .'_____`.   /___:______:___\   .'_____`.   ||
              || .-|---.---|-.   ||  _  _  ||   .-|---.---|-. ||
              || |=|   |   |=|   || | || | ||   |=|   |   |=| ||
              || |=|___|___|=|=  || | || | ||=  |=|___|___|=| ||
              || |=|~~~|~~~|=|   || | || | ||   |=|~~~|~~~|=| ||
              || |*|   |   |*|   || | || | ||  =|*|   |   |*| ||
              || |=|---|---|=| = || | || | ||   |=|---|---|=| ||
              || |=|   |   |=|   || | || | ||   |=|   |   |=| ||
              || `-|___|___|-'   ||o|_||_| ||   `-|___|___|-' ||
              ||  '---------`  = ||  _  _  || =  `---------'  ||
              || =   =           || | || | ||      =     =    ||
              ||  %@&   &@  =    || |_||_| ||  =   @&@   %@ = ||
              || %@&@% @%@&@    _||________||_   &@%&@ %&@&@  ||
              ||,,\\V//\\V//, _|___|------|___|_ ,\\V//\\V//,,||
              |--------------|____/--------\____|--------------|
             /- _  -  _   - _ -  _ - - _ - _ _ - _  _-  - _ - _ \
            /____________________________________________________\
     */
    static Path createDebuggablePath(Path path) {
        return new Path(List.of(), BlockPos.ORIGIN, false) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void toBuf(PacketByteBuf buf) {
                buf.writeBoolean(path.reachesTarget());
                buf.writeInt(path.getCurrentNodeIndex());
                buf.writeBlockPos(path.getTarget());

                Set<PathNode> open = new HashSet<>();
                Set<PathNode> closed = new HashSet<>();
                Set<TargetPathNode> targets = new HashSet<>();

                buf.writeCollection(IntStreams.range(path.getLength()).mapToObj(path::getNode).peek(node -> {
                    ((Set)(node instanceof TargetPathNode ? targets : node.visited ? closed : open)).add(node instanceof TargetPathNode t ? t : node);
                }).toList(), (bufx, node) -> node.write(bufx));


                new Path.DebugNodeInfo(open.toArray(PathNode[]::new), closed.toArray(PathNode[]::new), targets).write(buf);
            }
        };
    }
}
