package com.botamochi.rcap.network;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanySavedData;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RcapServerPackets {

    public static final Identifier UPDATE_COMPANY = new Identifier("rcap", "update_company");

    public static final Identifier OPEN_COMPANY_GUI = new Identifier("rcap", "open_company_gui");

    public static void sendOpenCompanyGui(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, OPEN_COMPANY_GUI, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_COMPANY, (server, player, handler, buf, sender) -> {
            long id = buf.readLong();
            String name = buf.readString();
            int color = buf.readInt();

            server.execute(() -> {
                CompanySavedData data = CompanySavedData.get(player.getWorld());
                for (Company c : data.companies) {
                    if (c.id == id) {
                        c.name = name;
                        c.color = color;
                        data.markDirty();
                        break;
                    }
                }
            });
        });
    }
}
