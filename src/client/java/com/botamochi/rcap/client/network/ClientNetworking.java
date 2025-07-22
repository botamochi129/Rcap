package com.botamochi.rcap.client.network;

import com.botamochi.rcap.data.Company;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class ClientNetworking {
    public static final Identifier UPDATE_COMPANY = new Identifier("rcap", "update_company");
    public static final Identifier DELETE_COMPANY = new Identifier("rcap", "delete_company");
    public static final Identifier CREATE_COMPANY = new Identifier("rcap", "create_company");

    public static void sendUpdateCompany(Company company) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(company.id);
        buf.writeString(company.name);
        buf.writeInt(company.color);
        ClientPlayNetworking.send(UPDATE_COMPANY, buf);
    }

    public static void sendDeleteCompany(long id) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(id);
        ClientPlayNetworking.send(DELETE_COMPANY, buf);
    }

    public static void sendCreateCompany(Company company) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(company.id);
        buf.writeString(company.name);
        buf.writeInt(company.color);
        ClientPlayNetworking.send(CREATE_COMPANY, buf);
    }
}
