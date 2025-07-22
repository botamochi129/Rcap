package com.botamochi.rcap.client.network;

import com.botamochi.rcap.client.screen.CompanyManagerScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class RcapClientPackets {
    public static final Identifier OPEN_COMPANY_GUI = new Identifier("rcap", "open_company_gui");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(OPEN_COMPANY_GUI, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                CompanyManagerScreen.open();
            });
        });
    }
}
