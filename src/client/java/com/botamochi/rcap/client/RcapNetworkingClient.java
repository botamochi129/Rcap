package com.botamochi.rcap.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class RcapNetworkingClient {
    public static final Identifier SET_HOUSING_RESIDENTS = new Identifier("rcap", "set_housing_residents");
    public static final Identifier SET_OFFICE_EMPLOYEES = new Identifier("rcap", "set_office_employees");
    public static final Identifier SET_RIDING_PLATFORM = new Identifier("rcap", "set_riding_platform");

    public static void sendHousingResidents(BlockPos pos, int value) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(value);
        ClientPlayNetworking.send(SET_HOUSING_RESIDENTS, buf);
    }
    public static void sendOfficeEmployees(BlockPos pos, int value) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(value);
        ClientPlayNetworking.send(SET_OFFICE_EMPLOYEES, buf);
    }
    public static void sendRidingPlatform(BlockPos pos, int value) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(value);
        ClientPlayNetworking.send(SET_RIDING_PLATFORM, buf);
    }
}