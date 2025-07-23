package com.botamochi.rcap.client.network;

import com.botamochi.rcap.data.Company;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ClientNetworking {
    public static final Identifier UPDATE_PLATFORM_ID = new Identifier("rcap", "update_platform_id");

    public static void sendUpdatePlatformIdPacket(BlockPos pos, long platformId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeLong(platformId);
        ClientPlayNetworking.send(UPDATE_PLATFORM_ID, buf);
    }
}
