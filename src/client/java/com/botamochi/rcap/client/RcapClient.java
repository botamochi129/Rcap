package com.botamochi.rcap.client;

import com.botamochi.rcap.network.PassengerSyncS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class RcapClient implements ClientModInitializer {
    public static final Identifier PASSENGER_SYNC = new Identifier("rcap", "passenger_sync");

    @Override
    public void onInitializeClient() {
        PassengerRenderer.init();
        ClientPlayNetworking.registerGlobalReceiver(PASSENGER_SYNC, (client, handler, buf, responseSender) -> {
            PassengerSyncS2CPacket.read(buf, PassengerClientCache.PASSENGERS);
        });
    }
}