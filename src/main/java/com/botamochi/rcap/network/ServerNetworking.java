package com.botamochi.rcap.network;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ServerNetworking {
    public static final Identifier UPDATE_COMPANY = new Identifier("rcap", "update_company");
    public static final Identifier DELETE_COMPANY = new Identifier("rcap", "delete_company");
    public static final Identifier CREATE_COMPANY = new Identifier("rcap", "create_company");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_COMPANY, (server, player, handler, buf, responseSender) -> {
            long id = buf.readLong();
            String name = buf.readString();
            int color = buf.readInt();
            server.execute(() -> {
                Company company = CompanyManager.getById(id);
                if (company != null) {
                    company.name = name;
                    company.color = color;
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(DELETE_COMPANY, (server, player, handler, buf, responseSender) -> {
            int id = buf.readInt();
            server.execute(() -> CompanyManager.removeById(id));
        });

        ServerPlayNetworking.registerGlobalReceiver(CREATE_COMPANY, (server, player, handler, buf, responseSender) -> {
            int id = buf.readInt();
            String name = buf.readString();
            int color = buf.readInt();
            server.execute(() -> {
                if (CompanyManager.getById(id) == null) {
                    CompanyManager.COMPANY_LIST.add(new Company(id, name, color));
                }
            });
        });
    }
}
