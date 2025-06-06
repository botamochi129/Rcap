// PassengerSyncPacket.java
package com.botamochi.rcap.network;

import com.botamochi.rcap.passenger.Passenger;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class PassengerSyncPacket {
    public static final Identifier ID = new Identifier("rcap", "passenger_sync");

    public final long passengerId;
    public final double x, y, z;
    public final boolean isRemoved;

    public PassengerSyncPacket(long passengerId, double x, double y, double z, boolean isRemoved) {
        this.passengerId = passengerId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isRemoved = isRemoved;
    }

    public PassengerSyncPacket(PacketByteBuf buf) {
        this.passengerId = buf.readLong();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.isRemoved = buf.readBoolean();
    }

    public void write(PacketByteBuf buf) {
        buf.writeLong(passengerId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(isRemoved);
    }

    // 送信ヘルパー
    public static void sendToAll(ServerWorld world, Passenger passenger, boolean isRemoved) {
        PacketByteBuf buf = PacketByteBufs.create();
        new PassengerSyncPacket(
                passenger.getId(), passenger.pos.x, passenger.pos.y, passenger.pos.z, isRemoved
        ).write(buf);
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, ID, buf);
        }
    }
}
